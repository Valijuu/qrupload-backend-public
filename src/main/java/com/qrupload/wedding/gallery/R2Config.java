package com.qrupload.wedding.gallery;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty("gallery.storage.endpoint")
@EnableConfigurationProperties(R2Properties.class)
public class R2Config {

    @Bean
    public GalleryStorage galleryStorage(R2Properties properties) {
        return new R2GalleryStorage(properties);
    }
}
