package com.qrupload.wedding.gallery;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GalleryExceptionHandler {

    static final String UNAVAILABLE_MESSAGE = "Die Galerie ist gerade nicht erreichbar. Bitte spaeter erneut versuchen.";

    static final String MESSAGE_FIELD = "message";

    @ExceptionHandler(GalleryNotFoundException.class)
    public ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    // Interne Details (Bucket, Endpunkt) gehoeren ins Log, nie in die Antwort
    @ExceptionHandler(GalleryStorageException.class)
    public ResponseEntity<Map<String, String>> storageUnavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(MESSAGE_FIELD, UNAVAILABLE_MESSAGE));
    }
}
