package com.qrupload.wedding.service;

import com.qrupload.wedding.config.GalleryProperties;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class PasswordVerifier {

    private final byte[] expectedPassword;

    public PasswordVerifier(GalleryProperties properties) {
        this.expectedPassword = properties.password().getBytes(StandardCharsets.UTF_8);
    }

    // Vergleich in konstanter Zeit, damit die Antwortzeit nichts ueber das Passwort verraet
    public boolean matches(String candidate) {
        if (candidate == null) {
            return false;
        }
        return MessageDigest.isEqual(expectedPassword, candidate.getBytes(StandardCharsets.UTF_8));
    }
}
