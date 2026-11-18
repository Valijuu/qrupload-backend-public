package com.qrupload.wedding.gallery;

import com.qrupload.wedding.support.GalleryTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.Duration;

import static com.qrupload.wedding.support.TestValues.PROTECTED_PATH;
import static com.qrupload.wedding.support.TestValues.loginSessionCookie;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GalleryTest
@Import(GalleryStorageFailureTest.FailingStorageConfig.class)
class GalleryStorageFailureTest {

    private static final String INTERNAL_DETAIL = "r2-konto-intern.cloudflarestorage.com";
    private static final String LOGIN_IP = "10.6.0.1";

    @TestConfiguration
    static class FailingStorageConfig {
        @Bean
        @Primary
        GalleryStorage failingStorage() {
            return new GalleryStorage() {
                @Override
                public GalleryIndex loadIndex() {
                    throw new GalleryStorageException("Bucket nicht erreichbar: " + INTERNAL_DETAIL);
                }

                @Override
                public URI presignView(String objectKey, Duration validity) {
                    throw new GalleryStorageException(INTERNAL_DETAIL);
                }

                @Override
                public URI presignDownload(String objectKey, Duration validity, String downloadFileName) {
                    throw new GalleryStorageException(INTERNAL_DETAIL);
                }
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void bucketAusfallLiefert503OhneInterneDetails() throws Exception {
        Cookie session = loginSessionCookie(mockMvc, LOGIN_IP);

        mockMvc.perform(get(PROTECTED_PATH).cookie(session))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(not(containsString(INTERNAL_DETAIL))));
    }
}
