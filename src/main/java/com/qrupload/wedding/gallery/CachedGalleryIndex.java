package com.qrupload.wedding.gallery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Haelt den Galerie-Index kurz im Speicher, damit nicht jede Anfrage den Bucket liest.
 * Faellt der Bucket aus, bleibt der zuletzt geladene Index sichtbar, und ein neuer Versuch
 * kommt erst nach einer Pause, damit sich nicht alle Anfragen an einem haengenden Bucket stauen.
 */
@Service
public class CachedGalleryIndex {

    static final Duration TIME_TO_LIVE = Duration.ofMinutes(10);
    static final Duration RETRY_PAUSE = Duration.ofSeconds(30);

    private static final Logger LOG = LoggerFactory.getLogger(CachedGalleryIndex.class);

    private final GalleryStorage storage;
    private final Clock clock;

    private GalleryIndex index;
    private Instant nextLoadAt = Instant.MIN;
    private GalleryStorageException lastFailure;

    public CachedGalleryIndex(GalleryStorage storage, Clock clock) {
        this.storage = storage;
        this.clock = clock;
    }

    public synchronized GalleryIndex get() {
        Instant now = clock.instant();
        if (now.isBefore(nextLoadAt)) {
            return currentOrFailure();
        }
        try {
            index = storage.loadIndex();
            lastFailure = null;
            nextLoadAt = now.plus(TIME_TO_LIVE);
        } catch (GalleryStorageException e) {
            LOG.warn("Galerie-Index nicht ladbar, naechster Versuch in {}", RETRY_PAUSE, e);
            lastFailure = e;
            nextLoadAt = now.plus(RETRY_PAUSE);
        }
        return currentOrFailure();
    }

    private GalleryIndex currentOrFailure() {
        if (index == null) {
            throw lastFailure;
        }
        return index;
    }
}
