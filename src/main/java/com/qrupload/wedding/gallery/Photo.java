package com.qrupload.wedding.gallery;

import java.util.Objects;

/** Ein Foto der Galerie: stabile Kennung und Dateiname des Originals. */
public record Photo(String id, String file) {

    public Photo {
        Objects.requireNonNull(id, "Foto ohne id im Galerie-Index");
        Objects.requireNonNull(file, "Foto ohne file im Galerie-Index");
    }
}
