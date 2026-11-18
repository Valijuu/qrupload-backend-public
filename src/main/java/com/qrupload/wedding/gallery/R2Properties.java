package com.qrupload.wedding.gallery;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Zugang zum privaten Cloudflare-R2-Bucket (S3-kompatibel), kommt aus Umgebungsvariablen/Secrets. */
@ConfigurationProperties("gallery.storage")
public record R2Properties(String endpoint, String bucket, String accessKey, String secretKey) {
}
