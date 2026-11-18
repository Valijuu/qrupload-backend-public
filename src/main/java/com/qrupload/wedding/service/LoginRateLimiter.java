package com.qrupload.wedding.service;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Begrenzt Login-Versuche pro Client-IP in einem festen Zeitfenster (im Speicher).
 * Jeder Versuch wird vor der Passwortpruefung reserviert, sonst koennten parallele Anfragen das
 * Limit ueberschreiten. Erfolgreiche Logins geben ihren Versuch wieder frei: Viele Gaeste teilen
 * sich oft eine IP (Hotel-WLAN, Mobilfunk-NAT).
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

    /** Reserviert einen Versuch. Liefert die Wartezeit, wenn das Limit erreicht ist, sonst leer. */
    public Optional<Duration> tryAcquire(String clientIp) {
        Instant now = clock.instant();
        windows.values().removeIf(window -> window.isExpiredAt(now));
        boolean[] granted = {false};
        Window window = windows.compute(clientIp, (ip, existing) -> {
            Window current = (existing == null || existing.isExpiredAt(now)) ? new Window(now, 0) : existing;
            if (current.attempts() >= MAX_ATTEMPTS_PER_WINDOW) {
                return current;
            }
            // Die Entscheidung faellt innerhalb der atomaren Operation, nicht danach
            granted[0] = true;
            return current.withAttempts(current.attempts() + 1);
        });
        return granted[0] ? Optional.empty() : Optional.of(Duration.between(now, window.endsAt()));
    }

    /** Gibt einen reservierten Versuch nach erfolgreichem Login wieder frei. */
    public void release(String clientIp) {
        Instant now = clock.instant();
        windows.computeIfPresent(clientIp, (ip, window) ->
                window.isExpiredAt(now) || window.attempts() == 0 ? window : window.withAttempts(window.attempts() - 1));
    }

    private record Window(Instant start, int attempts) {

        Instant endsAt() {
            return start.plus(WINDOW);
        }

        boolean isExpiredAt(Instant now) {
            return !now.isBefore(endsAt());
        }

        Window withAttempts(int newAttempts) {
            return new Window(start, newAttempts);
        }
    }
}
