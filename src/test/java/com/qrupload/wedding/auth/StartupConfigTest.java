package com.qrupload.wedding.auth;

import com.qrupload.wedding.support.GalleryTest;
import com.qrupload.wedding.config.GalleryProperties;
import com.qrupload.wedding.service.ClientIpResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

class StartupConfigTest {

    @Configuration
    @Import(GalleryProperties.class)
    static class GalleryPropertiesConfig {
    }

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withUserConfiguration(GalleryPropertiesConfig.class)
                    // Test soll unabhaengig von einer gesetzten GALLERY_PASSWORD-Umgebungsvariable sein
                    .withInitializer(context -> context.getEnvironment().getPropertySources().remove("systemEnvironment"));

    @Test
    void anwendungStartetNichtOhneGalleryPassword() {
        runner.run(context -> assertThat(context).hasFailed());
    }

    @Test
    void anwendungStartetNichtBeiLeeremGalleryPassword() {
        runner.withPropertyValues("gallery.password=  ")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void anwendungStartetNichtMitWenigerAlsEinemVertrautenProxy() {
        runner.withPropertyValues("gallery.password=geheim", "gallery.trusted-proxy-hops=0")
                .withUserConfiguration(ClientIpResolver.class)
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void vertrauteProxysHabenStandardEins() {
        runner.withPropertyValues("gallery.password=geheim")
                .withUserConfiguration(ClientIpResolver.class)
                .run(context -> assertThat(context).hasNotFailed());
    }

    @GalleryTest
    static class OhneAccessToken {

        @org.springframework.beans.factory.annotation.Autowired
        private org.springframework.context.ApplicationContext context;

        @Test
        void anwendungStartetOhneAccessToken() {
            assertThat(context).isNotNull();
        }
    }
}
