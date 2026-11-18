package com.qrupload.wedding.gallery;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** Haelt den Galerie-Index kurz im Speicher, damit nicht jede Anfrage den Bucket liest. */
@Service
public class CachedGalleryIndex {

    static final Duration TIME_TO_LIVE = Duration.ofMinutes(10);

    private final GalleryStorage storage;
    private final Clock clock;

    private GalleryIndex index;
    private Instant loadedAt;

    public CachedGalleryIndex(GalleryStorage storage, Clock clock) {
        this.storage = storage;
        this.clock = clock;
    }

    public synchronized GalleryIndex get() {
        Instant now = clock.instant();
        if (index == null || !now.isBefore(loadedAt.plus(TIME_TO_LIVE))) {
            index = storage.loadIndex();
            loadedAt = now;
        }
        return index;
    }
}
