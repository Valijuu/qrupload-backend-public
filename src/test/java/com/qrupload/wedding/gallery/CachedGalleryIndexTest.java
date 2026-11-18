package com.qrupload.wedding.gallery;

import com.qrupload.wedding.support.FakeGalleryStorage;
import com.qrupload.wedding.support.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CachedGalleryIndexTest {

    private static final Duration JUST_BEFORE_EXPIRY = Duration.ofMinutes(9);
    private static final Duration AFTER_EXPIRY = Duration.ofMinutes(11);

    private final FakeGalleryStorage storage = new FakeGalleryStorage();
    private final MutableClock clock = new MutableClock();
    private final CachedGalleryIndex cache = new CachedGalleryIndex(storage, clock);

    @Test
    void indexWirdInnerhalbDerZwischenspeicherzeitNurEinmalGeladen() {
        cache.get();
        clock.advance(JUST_BEFORE_EXPIRY);
        GalleryIndex index = cache.get();

        assertThat(index).isEqualTo(FakeGalleryStorage.INDEX);
        assertThat(storage.indexLoads()).isEqualTo(1);
    }

    @Test
    void indexWirdNachAblaufNeuGeladen() {
        cache.get();
        clock.advance(AFTER_EXPIRY);
        cache.get();

        assertThat(storage.indexLoads()).isEqualTo(2);
    }
}
