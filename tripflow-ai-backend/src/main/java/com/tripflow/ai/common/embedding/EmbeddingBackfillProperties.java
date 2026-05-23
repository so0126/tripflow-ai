package com.tripflow.ai.common.embedding;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.embedding.backfill")
public class EmbeddingBackfillProperties {
    private boolean enabled = false;
    private boolean force = false;
}
