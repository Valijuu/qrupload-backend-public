package com.qrupload.wedding.gallery;

/** Der Speicher (Bucket) ist nicht erreichbar oder liefert Unbrauchbares. */
public class GalleryStorageException extends RuntimeException {

    public GalleryStorageException(String message) {
        super(message);
    }

    public GalleryStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
