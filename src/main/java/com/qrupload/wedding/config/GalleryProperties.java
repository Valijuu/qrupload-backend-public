package com.qrupload.wedding.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record GalleryProperties(@Value(GalleryProperties.PASSWORD_PLACEHOLDER) String password) {

    static final String PASSWORD_PROPERTY_NAME = "gallery.password";

    // Leerer Default, damit die Validierung unten greift statt eines Spring-Platzhalterfehlers
    static final String PASSWORD_PLACEHOLDER = "${" + PASSWORD_PROPERTY_NAME + ":}";

    static final String MISSING_PASSWORD_MESSAGE = PASSWORD_PROPERTY_NAME + " darf nicht leer sein";

    public GalleryProperties {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(MISSING_PASSWORD_MESSAGE);
        }
        // Secrets enden gern auf einen Zeilenumbruch (echo ohne -n), das soll den Login nicht verhindern
        password = password.strip();
    }

    @Override
    public String toString() {
        return "GalleryProperties[password=***]";
    }
}
