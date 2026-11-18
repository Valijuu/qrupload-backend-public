package com.qrupload.wedding.gallery;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class GalleryIndexParser {

    static final String UNREADABLE_INDEX_MESSAGE = "Galerie-Index ist nicht lesbar";

    private final JsonMapper mapper = JsonMapper.builder().build();

    public GalleryIndex parse(String json) {
        try {
            return mapper.readValue(json, GalleryIndex.class);
        } catch (JacksonException e) {
            throw new GalleryStorageException(UNREADABLE_INDEX_MESSAGE, e);
        }
    }
}
