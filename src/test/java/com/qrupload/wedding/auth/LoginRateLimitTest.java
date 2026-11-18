package com.qrupload.wedding.auth;

import com.qrupload.wedding.support.MutableClock;
import com.qrupload.wedding.support.GalleryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.Duration;

import static com.qrupload.wedding.support.TestValues.PASSWORD;
import static com.qrupload.wedding.support.TestValues.WRONG_PASSWORD;
import static com.qrupload.wedding.support.TestValues.loginRequest;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GalleryTest
@Import(LoginRateLimitTest.ClockConfig.class)
class LoginRateLimitTest {

    private static final int ALLOWED_ATTEMPTS = 5;
    private static final long BLOCK_EXPIRED_AFTER_SECONDS = 61;
    private static final long ALMOST_FULL_WINDOW_MILLIS = 59_500;

    @TestConfiguration
    static class ClockConfig {
        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MutableClock clock;

    private ResultActions attempt(String forwardedFor, String password) throws Exception {
        return mockMvc.perform(loginRequest(forwardedFor, password));
    }

    private void useUpAttempts(String ip) throws Exception {
        for (int i = 0; i < ALLOWED_ATTEMPTS; i++) {
            attempt(ip, WRONG_PASSWORD).andExpect(status().isUnauthorized());
        }
    }

    @Test
    void fuenfVersucheProMinuteWerdenGeprueft() throws Exception {
        useUpAttempts("10.4.0.1");
    }

    @Test
    void sechsterVersuchLiefert429MitRetryAfter() throws Exception {
        useUpAttempts("10.4.0.2");
        attempt("10.4.0.2", WRONG_PASSWORD)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER));
    }

    @Test
    void retryAfterIstMindestensEineSekunde() throws Exception {
        useUpAttempts("10.4.0.9");
        clock.advance(Duration.ofMillis(ALMOST_FULL_WINDOW_MILLIS));
        attempt("10.4.0.9", WRONG_PASSWORD)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "1"));
    }

    @Test
    void nachSperreWirdAuchRichtigesPasswortAbgewiesen() throws Exception {
        useUpAttempts("10.4.0.3");
        attempt("10.4.0.3", PASSWORD).andExpect(status().isTooManyRequests());
    }

    @Test
    void andereIpIstUnabhaengig() throws Exception {
        useUpAttempts("10.4.0.4");
        attempt("10.4.0.5", PASSWORD).andExpect(status().isNoContent());
    }

    @Test
    void clientIpKommtAusErstemXForwardedForEintrag() throws Exception {
        useUpAttempts("10.4.0.6, 203.0.113.9");
        attempt("10.4.0.6, 198.51.100.7", WRONG_PASSWORD).andExpect(status().isTooManyRequests());
        attempt("10.4.0.7, 203.0.113.9", PASSWORD).andExpect(status().isNoContent());
    }

    @Test
    void nachEinerMinuteSindVersucheWiederMoeglich() throws Exception {
        useUpAttempts("10.4.0.8");
        attempt("10.4.0.8", PASSWORD).andExpect(status().isTooManyRequests());
        clock.advance(Duration.ofSeconds(BLOCK_EXPIRED_AFTER_SECONDS));
        attempt("10.4.0.8", PASSWORD).andExpect(status().isNoContent());
    }
}
