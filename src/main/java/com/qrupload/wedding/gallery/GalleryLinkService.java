package com.qrupload.wedding.gallery;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;

/** Loest Kennungen gegen den Index auf und erzeugt kurzlebige signierte Links. */
@Service
public class GalleryLinkService {

    static final Duration LINK_VALIDITY = Duration.ofMinutes(5);
    private static final String ZIP_KEY_PREFIX = "zips/";
    static final String UNKNOWN_VARIANT_MESSAGE = "Unbekannte Variante";
    static final String UNKNOWN_PHOTO_MESSAGE = "Unbekanntes Foto";
    static final String UNKNOWN_ZIP_MESSAGE = "Unbekanntes Paket";

    private final CachedGalleryIndex index;
    private final GalleryStorage storage;

    public GalleryLinkService(CachedGalleryIndex index, GalleryStorage storage) {
        this.index = index;
        this.storage = storage;
    }

    public URI photoLink(String photoId, String variantName) {
        PhotoVariant variant = PhotoVariant.fromPathName(variantName)
                .orElseThrow(() -> new GalleryNotFoundException(UNKNOWN_VARIANT_MESSAGE));
        Photo photo = index.get().photos().stream()
                .filter(candidate -> candidate.id().equals(photoId))
                .findFirst()
                .orElseThrow(() -> new GalleryNotFoundException(UNKNOWN_PHOTO_MESSAGE));
        String key = variant.objectKey(photo);
        if (variant.isDownload()) {
            return storage.presignDownload(key, LINK_VALIDITY, photo.file());
        }
        return storage.presignView(key, LINK_VALIDITY);
    }

    public URI zipLink(String zipName) {
        ZipPackage zip = index.get().zips().stream()
                .filter(candidate -> candidate.name().equals(zipName))
                .findFirst()
                .orElseThrow(() -> new GalleryNotFoundException(UNKNOWN_ZIP_MESSAGE));
        return storage.presignDownload(ZIP_KEY_PREFIX + zip.name(), LINK_VALIDITY, zip.name());
    }
}
