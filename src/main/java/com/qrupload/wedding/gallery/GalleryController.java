package com.qrupload.wedding.gallery;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Duration;

@RestController
public class GalleryController {

    static final String GALLERY_PATH = "/api/gallery";
    static final String PHOTO_PATH = "/api/photos/{id}/{variant}";
    static final String ZIP_PATH = "/api/zips/{name}";

    // Die Weiterleitung darf kurz im Browser bleiben, der signierte Link gilt laenger (5 Minuten)
    private static final Duration REDIRECT_MAX_AGE = Duration.ofSeconds(60);
    private static final CacheControl REDIRECT_CACHE = CacheControl.maxAge(REDIRECT_MAX_AGE).cachePrivate();

    private final CachedGalleryIndex index;
    private final GalleryLinkService links;

    public GalleryController(CachedGalleryIndex index, GalleryLinkService links) {
        this.index = index;
        this.links = links;
    }

    @GetMapping(GALLERY_PATH)
    public GalleryIndex gallery() {
        return index.get();
    }

    @GetMapping(PHOTO_PATH)
    public ResponseEntity<Void> photo(@PathVariable String id, @PathVariable String variant) {
        return redirectTo(links.photoLink(id, variant));
    }

    @GetMapping(ZIP_PATH)
    public ResponseEntity<Void> zip(@PathVariable String name) {
        return redirectTo(links.zipLink(name));
    }

    private ResponseEntity<Void> redirectTo(URI target) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, target.toString())
                .cacheControl(REDIRECT_CACHE)
                .build();
    }
}
