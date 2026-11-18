package com.qrupload.wedding.gallery;

import java.util.List;
import java.util.Objects;

public record GalleryIndex(List<Photo> photos, List<ZipPackage> zips) {

    public GalleryIndex {
        photos = List.copyOf(Objects.requireNonNull(photos, "Galerie-Index ohne photos"));
        zips = List.copyOf(Objects.requireNonNull(zips, "Galerie-Index ohne zips"));
    }
}
