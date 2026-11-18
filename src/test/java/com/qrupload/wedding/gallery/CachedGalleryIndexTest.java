package com.qrupload.wedding.gallery;

import com.qrupload.wedding.support.FakeGalleryStorage;
import com.qrupload.wedding.support.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CachedGalleryIndexTest {

    private static final Duration JUST_BEFORE_EXPIRY = Duration.ofMinutes(9);
    private static final Duration AFTER_EXPIRY = Duration.ofMinutes(11);
    private static final Duration WITHIN_RETRY_PAUSE = Duration.ofSeconds(10);
    private static final Duration AFTER_RETRY_PAUSE = Duration.ofSeconds(31);

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

    @Test
    void beiAusfallNachAblaufBleibtDerAlteIndexSichtbar() {
        cache.get();
        storage.failIndexLoads(true);
        clock.advance(AFTER_EXPIRY);

        assertThat(cache.get()).isEqualTo(FakeGalleryStorage.INDEX);
    }

    @Test
    void nachEinemAusfallWirdNichtBeiJederAnfrageNeuVersucht() {
        cache.get();
        storage.failIndexLoads(true);
        clock.advance(AFTER_EXPIRY);
        cache.get();
        clock.advance(WITHIN_RETRY_PAUSE);
        cache.get();

        assertThat(storage.indexLoads()).isEqualTo(2);

        storage.failIndexLoads(false);
        clock.advance(AFTER_RETRY_PAUSE);
        cache.get();
        assertThat(storage.indexLoads()).isEqualTo(3);
    }

    @Test
    void ohneBisherigenIndexWirdDerAusfallGemeldet() {
        storage.failIndexLoads(true);

        assertThatThrownBy(cache::get).isInstanceOf(GalleryStorageException.class);
    }

    @Test
    void auchOhneBisherigenIndexGiltDieWiederholungspause() {
        storage.failIndexLoads(true);
        assertThatThrownBy(cache::get).isInstanceOf(GalleryStorageException.class);
        clock.advance(WITHIN_RETRY_PAUSE);
        assertThatThrownBy(cache::get).isInstanceOf(GalleryStorageException.class);

        assertThat(storage.indexLoads()).isEqualTo(1);
    }
}
