package com.sanad.portal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/**
 * Application settings bound from {@code app.*}. Every value can be overridden with an
 * environment variable, e.g. {@code APP_ENVIRONMENT=production}, {@code APP_DATA_DIR=/var/lib/ccrp}.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String environment,
        String dataDir,
        Documents documents,
        Officer officer) {

    public AppProperties {
        if (environment == null || environment.isBlank()) environment = "local";
        if (dataDir == null || dataDir.isBlank()) dataDir = "./data";
        if (documents == null) documents = new Documents(null);
        if (officer == null) officer = new Officer(null, null);
    }

    public record Documents(DataSize maxSize) {
        public Documents {
            if (maxSize == null) maxSize = DataSize.ofMegabytes(2);
        }
    }

    /** Single reviewer account for the MVP. The password must come from the environment outside local/dev. */
    public record Officer(String username, String password) {
        public Officer {
            if (username == null || username.isBlank()) username = "officer";
        }
    }
}
