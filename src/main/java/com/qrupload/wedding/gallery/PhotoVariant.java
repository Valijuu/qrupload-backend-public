package com.qrupload.wedding.gallery;

import java.util.Arrays;
import java.util.Optional;

/** Die drei Fassungen eines Fotos im Bucket und wie ihr Objektschluessel entsteht. */
public enum PhotoVariant {
    THUMB("thumb", "thumbs/%s.jpg", false),
    MOBILE("mobile", "mobile/%s.jpg", false),
    ORIGINAL("original", "originals/%s", true);

    private final String pathName;
    private final String keyPattern;
    private final boolean download;

    PhotoVariant(String pathName, String keyPattern, boolean download) {
        this.pathName = pathName;
        this.keyPattern = keyPattern;
        this.download = download;
    }

    public static Optional<PhotoVariant> fromPathName(String pathName) {
        return Arrays.stream(values()).filter(variant -> variant.pathName.equals(pathName)).findFirst();
    }

    /** Thumbnail und Handy-Version heissen wie die Kennung, das Original behaelt seinen Dateinamen. */
    public String objectKey(Photo photo) {
        return keyPattern.formatted(this == ORIGINAL ? photo.file() : photo.id());
    }

    public boolean isDownload() {
        return download;
    }
}
