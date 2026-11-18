package com.qrupload.wedding.controller;

import com.qrupload.wedding.dto.LoginRequest;
import com.qrupload.wedding.service.ClientIpResolver;
import com.qrupload.wedding.service.LoginRateLimiter;
import com.qrupload.wedding.service.PasswordVerifier;
import com.qrupload.wedding.service.SessionCookieFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Optional;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    private static final String GUEST_SUBJECT = "Hochzeitsgast";
    private static final long MIN_RETRY_AFTER_SECONDS = 1;

    private final PasswordVerifier passwordVerifier;
    private final SessionCookieFactory cookieFactory;
    private final LoginRateLimiter rateLimiter;
    private final ClientIpResolver clientIpResolver;

    public LoginController(PasswordVerifier passwordVerifier, SessionCookieFactory cookieFactory,
                           LoginRateLimiter rateLimiter, ClientIpResolver clientIpResolver) {
        this.passwordVerifier = passwordVerifier;
        this.cookieFactory = cookieFactory;
        this.rateLimiter = rateLimiter;
        this.clientIpResolver = clientIpResolver;
    }

    @PostMapping
    public ResponseEntity<Void> login(@RequestBody(required = false) LoginRequest request,
                                      HttpServletRequest httpRequest) {
        String clientIp = clientIpResolver.resolve(httpRequest);
        Optional<Duration> retryAfter = rateLimiter.blockedFor(clientIp);
        if (retryAfter.isPresent()) {
            return tooManyAttempts(retryAfter.get());
        }
        if (request == null || !passwordVerifier.matches(request.password())) {
            rateLimiter.registerFailure(clientIp);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.create(GUEST_SUBJECT).toString())
                .build();
    }

    private static ResponseEntity<Void> tooManyAttempts(Duration waitTime) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(wholeSecondsRoundedUp(waitTime)))
                .build();
    }

    // Retry-After kennt nur ganze Sekunden; 0 wuerde den Client sofort erneut anfragen lassen
    private static long wholeSecondsRoundedUp(Duration duration) {
        long seconds = duration.toSeconds() + (duration.toNanosPart() > 0 ? 1 : 0);
        return Math.max(MIN_RETRY_AFTER_SECONDS, seconds);
    }
}
