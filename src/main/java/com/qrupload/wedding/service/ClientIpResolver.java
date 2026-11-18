package com.qrupload.wedding.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ClientIpResolver {

    private static final String FORWARDED_FOR = "X-Forwarded-For";
    private static final String SEPARATOR = ",";

    // Hinter Firebase Hosting und Cloud Run steht die Client-IP im ersten X-Forwarded-For-Eintrag
    public String resolve(HttpServletRequest request) {
        String forwardedFor = request.getHeader(FORWARDED_FOR);
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return request.getRemoteAddr();
        }
        return forwardedFor.split(SEPARATOR)[0].trim();
    }
}
