package com.qrupload.wedding.gallery;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/** Liest index.json; fehlende Pflichtfelder gelten wie kaputtes JSON als Speicherfehler. */
public class GalleryIndexParser {

    static final String UNREADABLE_INDEX_MESSAGE = "Galerie-Index ist nicht lesbar";

    private final JsonMapper mapper = JsonMapper.builder().build();

    public GalleryIndex parse(String json) {
        try {
            GalleryIndex index = mapper.readValue(json, GalleryIndex.class);
            if (index == null) {
                // Das JSON-Literal null ruft den Konstruktor nicht auf und waere sonst ein "leerer" Index
                throw new GalleryStorageException(UNREADABLE_INDEX_MESSAGE);
            }
            return index;
        } catch (GalleryStorageException e) {
            throw e;
        } catch (JacksonException e) {
            throw new GalleryStorageException(UNREADABLE_INDEX_MESSAGE, e);
        }
    }
}
