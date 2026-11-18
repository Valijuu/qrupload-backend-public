package com.qrupload.wedding.gallery;

import java.util.Objects;

public record ZipPackage(String name, long bytes, ZipKind kind) {

    public ZipPackage {
        Objects.requireNonNull(name, "Paket ohne name im Galerie-Index");
        Objects.requireNonNull(kind, "Paket ohne kind im Galerie-Index");
    }
}
