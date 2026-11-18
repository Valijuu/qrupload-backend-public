package com.qrupload.wedding.support;

import com.qrupload.wedding.gallery.GalleryIndex;
import com.qrupload.wedding.gallery.GalleryStorage;
import com.qrupload.wedding.gallery.Photo;
import com.qrupload.wedding.gallery.ZipKind;
import com.qrupload.wedding.gallery.ZipPackage;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Speicher-Attrappe: liefert einen festen Index und baut erkennbare "signierte" Links. */
public class FakeGalleryStorage implements GalleryStorage {

    public static final String BASE_URL = "https://bucket.test/";
    public static final String DOWNLOAD_PARAM = "?download=";

    public static final GalleryIndex INDEX = new GalleryIndex(
            List.of(new Photo("0001", "IMG_0001.jpg"), new Photo("0002", "IMG_0002.png")),
            List.of(new ZipPackage("originale-1.zip", 1_000_000_000L, ZipKind.ORIGINAL),
                    new ZipPackage("handy.zip", 500_000_000L, ZipKind.MOBILE)));

    private final AtomicInteger indexLoads = new AtomicInteger();
    private volatile Duration lastValidity;

    @Override
    public GalleryIndex loadIndex() {
        indexLoads.incrementAndGet();
        return INDEX;
    }

    @Override
    public URI presignView(String objectKey, Duration validity) {
        lastValidity = validity;
        return URI.create(BASE_URL + objectKey);
    }

    @Override
    public URI presignDownload(String objectKey, Duration validity, String downloadFileName) {
        lastValidity = validity;
        return URI.create(BASE_URL + objectKey + DOWNLOAD_PARAM + downloadFileName);
    }

    public int indexLoads() {
        return indexLoads.get();
    }

    public Duration lastValidity() {
        return lastValidity;
    }
}
