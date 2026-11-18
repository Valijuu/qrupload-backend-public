package com.qrupload.wedding.gallery;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class R2GalleryStorageTest {

    private static final Duration FIVE_MINUTES = Duration.ofMinutes(5);
    private static final String KEY = "thumbs/0001.jpg";

    private final R2GalleryStorage storage = new R2GalleryStorage(new R2Properties(
            "https://konto.r2.cloudflarestorage.com", "hochzeit", "AKIDEXAMPLE", "geheimes-secret-beispiel"));

    @Test
    void ansichtLinkEnthaeltObjektUndGueltigkeit() {
        URI link = storage.presignView(KEY, FIVE_MINUTES);

        assertThat(link.toString())
                .contains(KEY)
                .contains("X-Amz-Expires=300")
                .doesNotContain("response-content-disposition");
    }

    @Test
    void downloadLinkVerlangtAnhangMitDateiname() {
        URI link = storage.presignDownload("originals/IMG_0001.jpg", FIVE_MINUTES, "IMG_0001.jpg");

        assertThat(link.toString())
                .contains("response-content-disposition=attachment")
                .contains("IMG_0001.jpg");
    }

    @Test
    void linkEnthaeltNieDasSecret() {
        URI link = storage.presignView(KEY, FIVE_MINUTES);

        assertThat(link.toString()).doesNotContain("geheimes-secret-beispiel");
    }
}
