package com.tripflow.ai.common.embedding;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.tripflow.ai.common.chat.intent.dto.SeoulRegion;
import com.tripflow.ai.common.naver.NaverApiProperties;
import com.tripflow.ai.common.naver.dto.LocalItem;
import com.tripflow.ai.common.naver.dto.NaverLocalSearchResponse;
import com.tripflow.ai.planner.plan.util.CategoryNames;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.travel-place.import.naver", name = "enabled", havingValue = "true")
public class NaverTravelPlaceImportService {

    private static final int NAVER_LOCAL_DISPLAY_LIMIT = 5;

    private final WebClient webClient;
    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingModel embeddingModel;
    private final NaverApiProperties naverApiProperties;
    private final NaverImportProperties importProperties;

    public NaverTravelPlaceImportService(
            WebClient.Builder webClientBuilder,
            JdbcTemplate jdbcTemplate,
            EmbeddingModel embeddingModel,
            NaverApiProperties naverApiProperties,
            NaverImportProperties importProperties
    ) {
        this.webClient = webClientBuilder
                .baseUrl("https://openapi.naver.com")
                .defaultHeader("Accept", "application/json")
                .build();
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingModel = embeddingModel;
        this.naverApiProperties = naverApiProperties;
        this.importProperties = importProperties;
    }

    public void importTravelPlaces() {
        log.info("Naver travel place import started. targetCount={}", importProperties.getTargetCount());
        ensureCategories();

        int currentCount = countTravelPlaces();
        if (currentCount >= importProperties.getTargetCount()) {
            log.info("Naver travel place import skipped. currentCount={}, targetCount={}", currentCount, importProperties.getTargetCount());
            return;
        }

        Set<String> knownContentIds = new LinkedHashSet<>(jdbcTemplate.queryForList(
                "SELECT content_id FROM travel_places", String.class));

        int inserted = 0;
        int localCount = currentCount;

        outer:
        for (String query : buildSearchQueries()) {
            List<LocalItem> items = searchLocal(query);
            for (LocalItem item : items) {
                if (localCount >= importProperties.getTargetCount()) {
                    break outer;
                }
                TravelPlaceCandidate candidate = toCandidate(item);
                if (candidate == null || knownContentIds.contains(candidate.contentId())) {
                    continue;
                }
                float[] embedding = embeddingModel.embed(candidate.toEmbeddingText());
                upsertTravelPlace(candidate, embedding);
                knownContentIds.add(candidate.contentId());
                inserted++;
                localCount++;
            }

            if (inserted % 50 == 0 && inserted > 0) {
                log.info("Naver import progress. inserted={}, total={}", inserted, localCount);
            }
        }

        log.info("Naver travel place import finished. inserted={}, totalCount={}", inserted, localCount);
    }

    private List<LocalItem> searchLocal(String query) {
        try {
            NaverLocalSearchResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/search/local.json")
                            .queryParam("query", query)
                            .queryParam("display", NAVER_LOCAL_DISPLAY_LIMIT)
                            .queryParam("sort", "comment")
                            .build())
                    .header("X-Naver-Client-Id", naverApiProperties.getClientId())
                    .header("X-Naver-Client-Secret", naverApiProperties.getClientSecret())
                    .retrieve()
                    .bodyToMono(NaverLocalSearchResponse.class)
                    .block();

            if (response == null || response.getItems() == null) {
                return List.of();
            }
            return response.getItems();
        } catch (Exception e) {
            log.warn("Naver local search failed. query={}, error={}", query, e.getMessage());
            return List.of();
        }
    }

    private TravelPlaceCandidate toCandidate(LocalItem item) {
        String title = clean(item.getTitle());
        String roadAddress = clean(item.getRoadAddress());
        String jibunAddress = clean(item.getAddress());
        String address = roadAddress.isBlank() ? jibunAddress : roadAddress;
        String categoryText = clean(item.getCategory());
        String naverDescription = clean(item.getDescription());

        if (title.isBlank() || address.isBlank() || !address.contains("서울")) {
            return null;
        }

        String zoneId = resolveZoneId(address);
        if (zoneId == null) {
            return null;
        }

        String categoryCode = normalizeCategory(title, categoryText);
        Double lat = parseNaverCoordinate(item.getMapy());
        Double lng = parseNaverCoordinate(item.getMapx());
        String description = naverDescription.isBlank()
                ? "%s에 위치한 %s입니다.".formatted(address, title)
                : naverDescription;
        String categoryLabel = toCategoryLabel(categoryCode);
        String detailInfo = "%s 카테고리의 장소입니다. 네이버 분류: %s. 서울 %s 지역에 위치합니다."
                .formatted(categoryLabel, categoryText.isBlank() ? categoryLabel : categoryText, zoneId);
        String tags = toTagsJson(categoryCode, zoneId, categoryText);
        String contentId = "naver-local-" + sha256(title + "|" + address);
        String link = clean(item.getLink());

        return new TravelPlaceCandidate(
                contentId, title, address, link, zoneId,
                lat, lng, categoryCode, categoryText,
                description, detailInfo, tags
        );
    }

    private void upsertTravelPlace(TravelPlaceCandidate candidate, float[] embedding) {
        jdbcTemplate.update("""
                INSERT INTO travel_places (
                    content_id, title, address, tel, zone_id, first_image, first_image2,
                    lat, lng, category_code, description, tags, embedding, detail_info,
                    normalized_category, created_at, updated_at
                )
                VALUES (?, ?, ?, NULL, ?, NULL, NULL, ?, ?, ?, ?, ?::jsonb, CAST(? AS vector), ?, ?, now(), now())
                ON CONFLICT (content_id) DO UPDATE
                SET title               = EXCLUDED.title,
                    address             = EXCLUDED.address,
                    zone_id             = EXCLUDED.zone_id,
                    lat                 = EXCLUDED.lat,
                    lng                 = EXCLUDED.lng,
                    category_code       = EXCLUDED.category_code,
                    description         = EXCLUDED.description,
                    tags                = EXCLUDED.tags,
                    embedding           = EXCLUDED.embedding,
                    detail_info         = EXCLUDED.detail_info,
                    normalized_category = EXCLUDED.normalized_category,
                    updated_at          = now()
                """,
                candidate.contentId(),
                candidate.title(),
                candidate.address(),
                candidate.zoneId(),
                candidate.lat(),
                candidate.lng(),
                candidate.categoryCode(),
                candidate.description(),
                candidate.tags(),
                toPgVector(embedding),
                candidate.detailInfo(),
                candidate.categoryCode()
        );
    }

    private List<String> buildSearchQueries() {
        List<String> interests = List.of(
                "관광지", "맛집", "카페", "전시", "시장", "쇼핑", "공원", "박물관", "술집", "베이커리"
        );
        List<String> queries = new ArrayList<>();

        for (SeoulRegion region : SeoulRegion.values()) {
            for (String interest : interests) {
                queries.add("서울 " + region.getZoneId() + " " + interest);
            }
        }

        queries.addAll(List.of(
                "서울 인기 관광지",
                "서울 데이트 명소",
                "서울 가족 여행지",
                "서울 실내 관광지",
                "서울 야경 명소",
                "서울 로컬 맛집",
                "서울 감성 카페",
                "서울 전통시장",
                "서울 루프탑 카페",
                "서울 한옥 카페"
        ));

        return queries;
    }

    private void ensureCategories() {
        jdbcTemplate.update("""
                INSERT INTO travel_place_categories (code, name, parent_code, level, category_type)
                VALUES
                    ('ROOT',     '전체', NULL,   0, 'ROOT'),
                    ('SPOT',     '명소', 'ROOT', 1, 'PLACE'),
                    ('FOOD',     '맛집', 'ROOT', 1, 'PLACE'),
                    ('CAFE',     '카페', 'ROOT', 1, 'PLACE'),
                    ('SHOPPING', '쇼핑', 'ROOT', 1, 'PLACE'),
                    ('EVENT',    '행사', 'ROOT', 1, 'PLACE'),
                    ('STAY',     '숙소', 'ROOT', 1, 'PLACE'),
                    ('ETC',      '기타', 'ROOT', 1, 'PLACE')
                ON CONFLICT (code) DO UPDATE
                SET name          = EXCLUDED.name,
                    parent_code   = EXCLUDED.parent_code,
                    level         = EXCLUDED.level,
                    category_type = EXCLUDED.category_type
                """);
    }

    private int countTravelPlaces() {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM travel_places", Integer.class);
        return count == null ? 0 : count;
    }

    private String normalizeCategory(String title, String category) {
        String text = (title + " " + category).toLowerCase(Locale.ROOT);
        if (containsAny(text, "카페", "커피", "디저트", "베이커리", "브런치")) return CategoryNames.CAFE;
        if (containsAny(text, "음식", "맛집", "식당", "한식", "중식", "일식", "양식", "분식", "레스토랑", "술집", "고기", "치킨", "횟집")) return CategoryNames.FOOD;
        if (containsAny(text, "쇼핑", "백화점", "마트", "시장", "상가", "아울렛", "편집숍")) return CategoryNames.SHOPPING;
        if (containsAny(text, "호텔", "숙박", "게스트하우스", "모텔", "펜션")) return CategoryNames.STAY;
        if (containsAny(text, "공연", "전시", "축제", "행사", "뮤지컬", "영화")) return CategoryNames.EVENT;
        return CategoryNames.SPOT;
    }

    private String toCategoryLabel(String categoryCode) {
        return switch (categoryCode) {
            case CategoryNames.FOOD -> "맛집";
            case CategoryNames.CAFE -> "카페";
            case CategoryNames.SHOPPING -> "쇼핑";
            case CategoryNames.EVENT -> "전시·행사";
            case CategoryNames.STAY -> "숙소";
            default -> "명소";
        };
    }

    private String resolveZoneId(String address) {
        for (SeoulRegion region : SeoulRegion.values()) {
            if (address.contains(region.getZoneId())) {
                return region.getZoneId();
            }
            for (String keyword : region.keywords) {
                if (address.contains(keyword)) {
                    return region.getZoneId();
                }
            }
        }
        return null;
    }

    private String toTagsJson(String categoryCode, String zoneId, String categoryText) {
        String categoryLabel = toCategoryLabel(categoryCode);
        String externalCategory = categoryText.isBlank() ? categoryLabel : categoryText.replace("\"", "'");
        return "[\"%s\",\"%s\",\"%s\"]".formatted(categoryLabel, zoneId, externalCategory);
    }

    private String toPgVector(float[] embedding) {
        StringBuilder vector = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            float value = embedding[i];
            if (!Float.isFinite(value)) {
                throw new IllegalStateException("Embedding contains non-finite value at index " + i);
            }
            if (i > 0) vector.append(',');
            vector.append(value);
        }
        return vector.append(']').toString();
    }

    private boolean containsAny(String text, String... candidates) {
        for (String candidate : candidates) {
            if (text.contains(candidate)) return true;
        }
        return false;
    }

    private Double parseNaverCoordinate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Double.parseDouble(value) / 10_000_000D;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String clean(String value) {
        if (value == null) return "";
        return value
                .replaceAll("<[^>]*>", "")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim();
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                hex.append(String.format("%02x", hash[i]));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private record TravelPlaceCandidate(
            String contentId,
            String title,
            String address,
            String link,
            String zoneId,
            Double lat,
            Double lng,
            String categoryCode,
            String naverCategory,
            String description,
            String detailInfo,
            String tags
    ) {
        String toEmbeddingText() {
            return """
                    장소명: %s
                    주소: %s
                    지역: %s
                    카테고리: %s
                    네이버 분류: %s
                    설명: %s
                    상세정보: %s
                    태그: %s
                    """.formatted(title, address, zoneId, categoryCode, naverCategory, description, detailInfo, tags);
        }
    }
}
