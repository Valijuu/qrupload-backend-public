package com.qrupload.wedding.gallery;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ZipKind {
    ORIGINAL("original"),
    MOBILE("mobile");

    private final String jsonName;

    ZipKind(String jsonName) {
        this.jsonName = jsonName;
    }

    @JsonValue
    public String jsonName() {
        return jsonName;
    }
}
