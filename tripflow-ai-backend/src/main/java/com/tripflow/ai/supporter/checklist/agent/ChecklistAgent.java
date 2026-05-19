package com.tripflow.ai.supporter.checklist.agent;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import com.tripflow.ai.supporter.checklist.dao.ChecklistTravelDayDao;
import com.tripflow.ai.supporter.checklist.dto.entity.Checklist;
import com.tripflow.ai.supporter.checklist.dto.entity.ChecklistItem;
import com.tripflow.ai.supporter.checklist.dto.response.ChecklistItemResponse;
import com.tripflow.ai.supporter.checklist.dto.response.TravelDayResponse;
import com.tripflow.ai.supporter.checklist.service.ChecklistService;
import com.tripflow.ai.supporter.checklist.service.ChecklistItemService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChecklistAgent {

    private final ChatClient.Builder chatClientBuilder;
    private final ChecklistTravelDayDao checklistTravelDayDao;
    private final ChecklistService checklistService;
    private final ChecklistItemService checklistItemService;
    private final ObjectMapper objectMapper;

    public ChecklistItemResponse generateChecklist(Long planId, Integer dayIndex, Long userId) {
        log.info("📋 Generating checklist for planId: {}, dayIndex: {}, userId: {}", planId, dayIndex, userId);

        // 1) 여행 일정과 장소 조회
        TravelDayResponse travelDay = checklistTravelDayDao.getTravelDay(planId, dayIndex);

        if (travelDay == null || travelDay.getPlaces() == null || travelDay.getPlaces().isEmpty()) {
            log.warn("⚠️ No places found for planId: {}, dayIndex: {}", planId, dayIndex);
            return null;
        }

        log.info("📊 Travel day info - title: {}, date: {}", travelDay.getDayTitle(), travelDay.getPlanDate());
        log.info("📍 Total places: {}", travelDay.getPlaces().size());

        // 2) 장소 정보 상세 로깅
        StringBuilder placeDetails = new StringBuilder();
        for (TravelDayResponse.PlaceDto place : travelDay.getPlaces()) {
            placeDetails.append("\n[").append(place.getPlaceName()).append("]")
                    .append("\n  제목: ").append(place.getPlaceTitle())
                    .append("\n  주소: ").append(place.getAddress())
                    .append("\n  시간: ").append(place.getStartAt()).append(" ~ ").append(place.getEndAt())
                    .append("\n  위치: ").append(place.getLat()).append(", ").append(place.getLng())
                    .append("\n  예상비용: ").append(place.getExpectedCost()).append("\n");
        }
        log.info("📋 Place Details:{}", placeDetails.toString());

        // 3) LLM 호출
        ChatClient chatClient = chatClientBuilder.build();
        String llmResponse = chatClient.prompt()
        .system("""
                당신은 여행 정보 전문가입니다.
                여행객들을 위해 실용적이고 유용한 여행 팁을 생성해주세요.

                반환 형식: 반드시 JSON 형식으로만 응답하세요
                {
                  "title": "꼭 알아야 할 여행 팁",
                  "items": [
                    "장소명: 구체적인 팁",
                    ...
                  ]
                }

                규칙:
                1. 3개까지의 항목만 생성(그 미만이면 그만큼)
                2. 각 항목은 "장소명: 팁" 형식 (예: "경복궁: 한복 입으면 입장료 무료")
                3. 마크다운, 이모지 절대 금지
                4. JSON 외의 다른 텍스트는 포함하지 마세요
                5. **중복된 장소명은 절대 금지** - 각 장소는 정확히 한 번만 나타나야 함
                6. **제공된 장소를 골고루 포함**해야 함
                """)
        .user("""
                방문 날짜: """ + travelDay.getPlanDate() + """
                여행 일정: """ + travelDay.getDayTitle() + """

                방문 장소들:
                """ + travelDay.getPlaces().stream()
                .map(p -> p.getPlaceName())
                .collect(Collectors.joining(", ")) + """

                        위 장소들을 방문할 때 도움이 될 만한 팁을 생성해주세요.
                        
                        ⚠️ 중요: 위의 모든 장소를 정확히 한 번씩만 사용하세요!
                        같은 장소를 두 번 이상 반복하지 마세요.
                        
                        각 팁은 "장소명: 구체적인 팁" 형식으로 작성해주세요.

                        예시:
                        {
                          "title": "꼭 알아야 할 여행 팁",
                          "items": [
                            "경복궁: 한복 입으면 입장료 무료, 일반인 3,000원",
                            "N서울타워: 날씨 맑은 날 가야 야경 잘 보임, 저녁 6시 일몰+야경 동시 감상",
                            "한강공원: 돗자리 깔고 앉을 수 있음, 모기 방충제 필수",
                            "박물관: 목요일 야간 개방(20시까지), 현장 구매 시 10% 할인",
                            "명동: 쇼핑 후 커피는 필수"
                          ]
                        }

                        JSON 형식으로 응답해주세요.
                        """)
        .call()
        .content();

        log.info("🤖 LLM generated response (length: {})", llmResponse.length());
        log.debug("📄 Full response: {}", llmResponse);

        // 4) JSON 파싱
        ChecklistItemResponse result = null;
        try {
            String cleanJson = llmResponse
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .replaceAll("```", "")
                    .trim();

            int startIdx = cleanJson.indexOf('{');
            int endIdx = cleanJson.lastIndexOf('}');

            if (startIdx >= 0 && endIdx > startIdx) {
                cleanJson = cleanJson.substring(startIdx, endIdx + 1);
            }

            log.info("🧹 Cleaned JSON: {}", cleanJson);

            result = objectMapper.readValue(cleanJson, ChecklistItemResponse.class);

            log.info("✅ Generated {} checklist items",
                    result.getItems() != null ? result.getItems().size() : 0);

        } catch (Exception e) {
            log.error("❌ Error parsing LLM response", e);
            // null이 아닌 빈 response 생성
            result = new ChecklistItemResponse();
            result.setTitle("여행 필수 준비물");
            result.setItems(List.of());
        }

        // 5) DB에 저장
        saveChecklistToDb(planId, dayIndex, result, travelDay.getUserId());

        return result;
    }

    /**
     * 생성된 체크리스트를 DB에 저장
     */
    private void saveChecklistToDb(Long planId, Integer dayIndex, ChecklistItemResponse llmResponse, Long userId) {
        try {
            log.info("💾 Saving checklist to DB - userId: {}, dayIndex: {}", userId, dayIndex);

            // 0) 기존 데이터 삭제
            log.info("🗑️ Deleting existing checklist for userId: {}", userId);
            List<Checklist> existingChecklists = checklistService.findByUserId(userId);

            for (Checklist checklist : existingChecklists) {
                checklistItemService.deleteItemsByChecklistId(checklist.getId());
            }

            checklistService.deleteItemsByUserId(userId);
            log.info("✅ Existing data deleted");

            // 1) Checklist 생성
            Checklist checklist = Checklist.builder()
                    .userId(userId)
                    .dayIndex(dayIndex)
                    .createdAt(OffsetDateTime.now())
                    .build();

            Long checklistId = checklistService.create(checklist);
            log.info("✅ Checklist created with id: {}", checklistId);

            // 2) LLM 생성 항목 저장
            int llmItemCount = 0;
            if (llmResponse != null && llmResponse.getItems() != null && !llmResponse.getItems().isEmpty()) {
                for (String item : llmResponse.getItems()) {
                    ChecklistItem checklistItem = ChecklistItem.builder()
                            .checklistId(checklistId)
                            .content(item)
                            .category("LOCATION")
                            .isChecked(false)
                            .createdAt(OffsetDateTime.now())
                            .build();

                    checklistItemService.create(checklistItem);
                    log.debug("✅ LLM ChecklistItem created - content: {}", item);
                    llmItemCount++;
                }
                log.info("✅ Total {} LLM items saved", llmItemCount);
            } else {
                log.warn("⚠️ No LLM items to save");
            }

            // 3) 추가 고정 항목 저장
            List<ChecklistItem> fixedItems = List.of(
                    ChecklistItem.builder()
                            .checklistId(checklistId)
                            .content("날씨가 춥고 비가 올 가능성이 있으니 우산을 챙기고 옷을 따뜻하게 입으세요")
                            .category("WEATHER")
                            .isChecked(false)
                            .createdAt(OffsetDateTime.now())
                            .build(),
                    ChecklistItem.builder()
                            .checklistId(checklistId)
                            .content("교통카드를 반드시 챙기고 교통카드 충전을 위한 현금도 챙기세요")
                            .category("GENERAL")
                            .isChecked(false)
                            .createdAt(OffsetDateTime.now())
                            .build(),
                    ChecklistItem.builder()
                            .checklistId(checklistId)
                            .content("보조 배터리를 챙기고 휴대폰도 충분하게 충전해두세요")
                            .category("GENERAL")
                            .isChecked(false)
                            .createdAt(OffsetDateTime.now())
                            .build()
            );

            for (ChecklistItem item : fixedItems) {
                checklistItemService.create(item);
                log.debug("✅ Fixed ChecklistItem created - content: {}, category: {}", item.getContent(), item.getCategory());
            }

            log.info("✅ Total {} fixed items saved", fixedItems.size());
            log.info("🎉 Checklist saved successfully to DB (LLM: {} items + Fixed: {} items = Total: {} items)",
                    llmItemCount, fixedItems.size(), (llmItemCount + fixedItems.size()));

        } catch (Exception e) {
            log.error("❌ Error saving checklist to DB", e);
        }
    }
}