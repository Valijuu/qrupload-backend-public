package com.qrupload.wedding.auth;

import com.qrupload.wedding.config.GalleryProperties;
import com.qrupload.wedding.service.PasswordVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordVerifierTest {

    @Test
    void zeilenumbruchAmEndeDesSecretsStoertNicht() {
        // Ein mit "echo" ohne -n angelegtes Secret endet auf einen Zeilenumbruch
        PasswordVerifier verifier = new PasswordVerifier(new GalleryProperties("geheim\n"));

        assertThat(verifier.matches("geheim")).isTrue();
    }

    @Test
    void passwortErscheintNichtInToString() {
        assertThat(new GalleryProperties("geheim").toString()).doesNotContain("geheim");
    }
}
