package com.qrupload.wedding.auth;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gemeinsame Test-Konfiguration: voller Spring-Kontext mit MockMvc und festen Test-Werten,
 * unabhaengig von Umgebungsvariablen.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(properties = {
        "jwt.secret=" + TestValues.JWT_SECRET,
        "gallery.password=" + TestValues.PASSWORD
})
@AutoConfigureMockMvc
public @interface GalleryTest {
}
