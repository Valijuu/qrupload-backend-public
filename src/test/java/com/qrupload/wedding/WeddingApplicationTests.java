package com.qrupload.wedding;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static com.qrupload.wedding.support.TestValues.JWT_SECRET;
import static com.qrupload.wedding.support.TestValues.PASSWORD;

/** Startet die Anwendung mit der echten R2-Verdrahtung (Beispielwerte, kein Netzwerkzugriff beim Start). */
@SpringBootTest(properties = {
        "jwt.secret=" + JWT_SECRET,
        "gallery.password=" + PASSWORD,
        "gallery.storage.endpoint=https://konto.r2.cloudflarestorage.com",
        "gallery.storage.bucket=wedding",
        "gallery.storage.access-key=AKIDEXAMPLE",
        "gallery.storage.secret-key=beispiel-secret"
})
class WeddingApplicationTests {

    @Test
    void contextLoads() {
    }
}
