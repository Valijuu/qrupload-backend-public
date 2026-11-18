package com.qrupload.wedding.service;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Begrenzt fehlgeschlagene Login-Versuche pro Client-IP in einem festen Zeitfenster (im Speicher).
 * Erfolgreiche Logins zaehlen nicht: Viele Gaeste teilen sich oft eine IP (Hotel-WLAN, Mobilfunk-NAT).
 */
@Service
public class LoginRateLimiter {

    static final int MAX_ATTEMPTS_PER_WINDOW = 5;
    static final Duration WINDOW = Duration.ofMinutes(1);

    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public LoginRateLimiter(Clock clock) {
        this.clock = clock;
    }

    /** Liefert die Wartezeit, wenn diese IP gesperrt ist, sonst leer. */
    public Optional<Duration> blockedFor(String clientIp) {
        Instant now = clock.instant();
        Window window = windows.get(clientIp);
        if (window == null || window.isExpiredAt(now) || window.attempts() < MAX_ATTEMPTS_PER_WINDOW) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(now, window.endsAt()));
    }

    /** Zaehlt einen Fehlversuch dieser IP. */
    public void registerFailure(String clientIp) {
        Instant now = clock.instant();
        windows.values().removeIf(window -> window.isExpiredAt(now));
        windows.compute(clientIp, (ip, existing) -> {
            Window current = (existing == null || existing.isExpiredAt(now)) ? new Window(now, 0) : existing;
            return current.withAttempt();
        });
    }

    private record Window(Instant start, int attempts) {

        Instant endsAt() {
            return start.plus(WINDOW);
        }

        boolean isExpiredAt(Instant now) {
            return !now.isBefore(endsAt());
        }

        Window withAttempt() {
            return new Window(start, attempts + 1);
        }
    }
}
