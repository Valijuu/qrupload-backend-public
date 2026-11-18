package com.qrupload.wedding.service;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class SessionCookieFactory {

    // Firebase Hosting reicht nur dieses Cookie an Cloud Run durch
    public static final String COOKIE_NAME = "__session";

    private static final String COOKIE_PATH = "/";
    private static final String SAME_SITE = "Lax";

    private final JwtService jwtService;

    public SessionCookieFactory(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public ResponseCookie create(String subject) {
        return ResponseCookie.from(COOKIE_NAME, jwtService.generateToken(subject))
                .httpOnly(true)
                .secure(true)
                .sameSite(SAME_SITE)
                .path(COOKIE_PATH)
                .maxAge(JwtService.LIFETIME)
                .build();
    }
}
