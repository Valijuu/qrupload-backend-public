package com.qrupload.wedding.gallery;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GalleryIndexParserTest {

    private static final String INDEX_JSON = """
            {"photos":[{"id":"0001","file":"IMG_0001.jpg"},{"id":"0002","file":"IMG_0002.png"}],
             "zips":[{"name":"originale-1.zip","bytes":1000000000,"kind":"original"},
                     {"name":"handy.zip","bytes":500000000,"kind":"mobile"}]}
            """;

    private final GalleryIndexParser parser = new GalleryIndexParser();

    @Test
    void liestFotosUndPaketeAusDemIndex() {
        GalleryIndex index = parser.parse(INDEX_JSON);

        assertThat(index.photos()).containsExactly(
                new Photo("0001", "IMG_0001.jpg"), new Photo("0002", "IMG_0002.png"));
        assertThat(index.zips()).containsExactly(
                new ZipPackage("originale-1.zip", 1_000_000_000L, ZipKind.ORIGINAL),
                new ZipPackage("handy.zip", 500_000_000L, ZipKind.MOBILE));
    }

    @Test
    void kaputterIndexWirdAlsSpeicherfehlerGemeldet() {
        assertThatThrownBy(() -> parser.parse("{kein json"))
                .isInstanceOf(GalleryStorageException.class);
    }

    @Test
    void indexOhnePaketlisteWirdAlsSpeicherfehlerGemeldet() {
        assertThatThrownBy(() -> parser.parse("{\"photos\":[]}"))
                .isInstanceOf(GalleryStorageException.class);
    }

    @Test
    void fotoOhneKennungWirdAlsSpeicherfehlerGemeldet() {
        assertThatThrownBy(() -> parser.parse("{\"photos\":[{\"file\":\"a.jpg\"}],\"zips\":[]}"))
                .isInstanceOf(GalleryStorageException.class);
    }

    @Test
    void paketOhneArtWirdAlsSpeicherfehlerGemeldet() {
        assertThatThrownBy(() -> parser.parse("{\"photos\":[],\"zips\":[{\"name\":\"a.zip\",\"bytes\":1}]}"))
                .isInstanceOf(GalleryStorageException.class);
    }

    @Test
    void jsonNullStattIndexWirdAlsSpeicherfehlerGemeldet() {
        assertThatThrownBy(() -> parser.parse("null")).isInstanceOf(GalleryStorageException.class);
    }

    @Test
    void indexMitLeerenListenIstGueltig() {
        GalleryIndex index = parser.parse("{\"photos\":[],\"zips\":[]}");

        assertThat(index.photos()).isEmpty();
        assertThat(index.zips()).isEmpty();
    }
}
