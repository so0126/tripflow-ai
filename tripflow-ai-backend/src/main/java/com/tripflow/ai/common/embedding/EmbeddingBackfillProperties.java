package com.tripflow.ai.common.embedding;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.embedding.backfill")
public record EmbeddingBackfillProperties(
        boolean enabled,
        boolean force,
        boolean exitOnComplete
) {
}
