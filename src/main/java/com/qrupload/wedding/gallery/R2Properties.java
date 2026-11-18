package com.qrupload.wedding.gallery;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Zugang zum privaten Cloudflare-R2-Bucket (S3-kompatibel), kommt aus Umgebungsvariablen/Secrets. */
@ConfigurationProperties("gallery.storage")
public record R2Properties(String endpoint, String bucket, String accessKey, String secretKey) {

    static final String MISSING_SETTING_MESSAGE = "gallery.storage.%s fehlt";

    public R2Properties {
        requirePresent(endpoint, "endpoint");
        requirePresent(bucket, "bucket");
        requirePresent(accessKey, "access-key");
        requirePresent(secretKey, "secret-key");
    }

    private static void requirePresent(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(MISSING_SETTING_MESSAGE.formatted(name));
        }
    }

    // Zugangsdaten nie in Logs oder Fehlermeldungen
    @Override
    public String toString() {
        return "R2Properties[endpoint=" + endpoint + ", bucket=" + bucket + ", accessKey=***, secretKey=***]";
    }
}
