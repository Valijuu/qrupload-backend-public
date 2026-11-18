package com.qrupload.wedding.auth;

import com.qrupload.wedding.service.LoginRateLimiter;
import com.qrupload.wedding.support.MutableClock;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterTest {

    private static final String IP = "192.0.2.10";
    private static final int ALLOWED_ATTEMPTS = 5;
    private static final int PARALLEL_REQUESTS = 50;
    private static final long PARALLEL_WAIT_SECONDS = 10;

    private final LoginRateLimiter limiter = new LoginRateLimiter(new MutableClock());

    @Test
    void parallelAnfragenEinerIpDuerfenDasLimitNichtUeberschreiten() throws Exception {
        AtomicInteger passwordChecks = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(PARALLEL_REQUESTS);
        try {
            for (int i = 0; i < PARALLEL_REQUESTS; i++) {
                pool.submit(() -> {
                    start.await();
                    if (limiter.tryAcquire(IP).isEmpty()) {
                        passwordChecks.incrementAndGet();
                    }
                    return null;
                });
            }
            start.countDown();
        } finally {
            pool.shutdown();
            assertThat(pool.awaitTermination(PARALLEL_WAIT_SECONDS, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(passwordChecks.get()).isEqualTo(ALLOWED_ATTEMPTS);
    }

    @Test
    void freigegebenerVersuchZaehltNichtMehr() {
        for (int i = 0; i < ALLOWED_ATTEMPTS * 3; i++) {
            assertThat(limiter.tryAcquire(IP)).isEmpty();
            limiter.release(IP);
        }

        assertThat(limiter.tryAcquire(IP)).isEmpty();
    }

    @Test
    void gesperrteAnfragenZaehlenNichtWeiter() {
        for (int i = 0; i < ALLOWED_ATTEMPTS; i++) {
            limiter.tryAcquire(IP);
        }

        assertThat(limiter.tryAcquire(IP)).isPresent();
        assertThat(limiter.tryAcquire(IP)).isPresent();
    }
}
