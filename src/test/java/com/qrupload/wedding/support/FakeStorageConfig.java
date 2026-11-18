package com.qrupload.wedding.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FakeStorageConfig {

    @Bean
    public FakeGalleryStorage fakeGalleryStorage() {
        return new FakeGalleryStorage();
    }
}
