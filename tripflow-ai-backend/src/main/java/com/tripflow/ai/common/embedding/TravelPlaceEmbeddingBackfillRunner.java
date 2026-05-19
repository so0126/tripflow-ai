package com.tripflow.ai.common.embedding;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.embedding.backfill", name = "enabled", havingValue = "true")
public class TravelPlaceEmbeddingBackfillRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingModel embeddingModel;
    private final boolean force;
    private final boolean exitOnComplete;
    private final ConfigurableApplicationContext applicationContext;

    public TravelPlaceEmbeddingBackfillRunner(
            JdbcTemplate jdbcTemplate,
            EmbeddingModel embeddingModel,
            @Value("${app.embedding.backfill.force:false}") boolean force,
            @Value("${app.embedding.backfill.exit-on-complete:false}") boolean exitOnComplete,
            ConfigurableApplicationContext applicationContext
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingModel = embeddingModel;
        this.force = force;
        this.exitOnComplete = exitOnComplete;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        String sql = """
                SELECT id, title, address, normalized_category, description, detail_info, tags
                FROM travel_places
                %s
                ORDER BY id
                """.formatted(force ? "" : "WHERE embedding IS NULL");

        List<TravelPlaceEmbeddingSource> rows = jdbcTemplate.query(sql, (rs, rowNum) ->
                new TravelPlaceEmbeddingSource(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("address"),
                        rs.getString("normalized_category"),
                        rs.getString("description"),
                        rs.getString("detail_info"),
                        rs.getString("tags")
                )
        );

        if (rows.isEmpty()) {
            log.info("Travel place embedding backfill skipped. No rows to update.");
            closeIfNeeded();
            return;
        }

        log.info("Travel place embedding backfill started. count={}, force={}", rows.size(), force);
        int updated = 0;
        for (TravelPlaceEmbeddingSource row : rows) {
            float[] embedding = embeddingModel.embed(row.toEmbeddingText());
            jdbcTemplate.update(
                    "UPDATE travel_places SET embedding = CAST(? AS vector), updated_at = now() WHERE id = ?",
                    toPgVector(embedding),
                    row.id()
            );
            updated++;
        }

        log.info("Travel place embedding backfill finished. updated={}", updated);
        closeIfNeeded();
    }

    private String toPgVector(float[] embedding) {
        StringBuilder vector = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            float value = embedding[i];
            if (!Float.isFinite(value)) {
                throw new IllegalStateException("Embedding contains non-finite value at index " + i);
            }
            if (i > 0) {
                vector.append(',');
            }
            vector.append(value);
        }
        return vector.append(']').toString();
    }

    private void closeIfNeeded() {
        if (exitOnComplete) {
            log.info("Travel place embedding backfill requested application shutdown.");
            applicationContext.close();
        }
    }

    private record TravelPlaceEmbeddingSource(
            long id,
            String title,
            String address,
            String normalizedCategory,
            String description,
            String detailInfo,
            String tags
    ) {
        private String toEmbeddingText() {
            return """
                    장소명: %s
                    주소: %s
                    카테고리: %s
                    설명: %s
                    상세정보: %s
                    태그: %s
                    """.formatted(
                    safe(title),
                    safe(address),
                    safe(normalizedCategory),
                    safe(description),
                    safe(detailInfo),
                    safe(tags)
            );
        }

        private static String safe(String value) {
            return value == null ? "" : value;
        }
    }
}
