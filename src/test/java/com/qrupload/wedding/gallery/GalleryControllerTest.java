package com.qrupload.wedding.gallery;

import com.qrupload.wedding.support.FakeGalleryStorage;
import com.qrupload.wedding.support.GalleryTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


import static com.qrupload.wedding.support.FakeGalleryStorage.BASE_URL;
import static com.qrupload.wedding.support.FakeGalleryStorage.DOWNLOAD_PARAM;
import static com.qrupload.wedding.support.TestValues.PROTECTED_PATH;
import static com.qrupload.wedding.support.TestValues.loginSessionCookie;
import static com.qrupload.wedding.support.TestValues.uniqueIp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GalleryTest
class GalleryControllerTest {

    private static final String PHOTOS_PATH = "/api/photos/";
    private static final String ZIPS_PATH = "/api/zips/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FakeGalleryStorage storage;

    private Cookie session;

    @BeforeEach
    void login() throws Exception {
        session = loginSessionCookie(mockMvc, uniqueIp());
    }

    private ResultActions getWithSession(String path) throws Exception {
        return mockMvc.perform(get(path).cookie(session));
    }

    @Test
    void galerieListeEnthaeltFotosUndPakete() throws Exception {
        getWithSession(PROTECTED_PATH)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photos", hasSize(2)))
                .andExpect(jsonPath("$.photos[0].id").value("0001"))
                .andExpect(jsonPath("$.zips[0].name").value("originale-1.zip"))
                .andExpect(jsonPath("$.zips[0].bytes").value(1_000_000_000L))
                .andExpect(jsonPath("$.zips[0].kind").value("original"))
                .andExpect(jsonPath("$.zips[1].kind").value("mobile"));
    }

    @Test
    void galerieListeOhneAnmeldungLiefert401() throws Exception {
        mockMvc.perform(get(PROTECTED_PATH)).andExpect(status().isUnauthorized());
    }

    @Test
    void vorschauLeitetAufSignierteAnsichtWeiter() throws Exception {
        getWithSession(PHOTOS_PATH + "0001/thumb")
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, BASE_URL + "thumbs/0001.jpg"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "max-age=60, private"));
    }

    @Test
    void handyQualitaetLeitetAufMobileVersionWeiter() throws Exception {
        getWithSession(PHOTOS_PATH + "0002/mobile")
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, BASE_URL + "mobile/0002.jpg"));
    }

    @Test
    void originalWirdAlsDownloadMitOriginalnamenAusgeliefert() throws Exception {
        getWithSession(PHOTOS_PATH + "0002/original")
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION,
                        BASE_URL + "originals/IMG_0002.png" + DOWNLOAD_PARAM + "IMG_0002.png"));
    }

    @Test
    void zipWirdAlsDownloadAusgeliefert() throws Exception {
        getWithSession(ZIPS_PATH + "originale-1.zip")
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION,
                        BASE_URL + "zips/originale-1.zip" + DOWNLOAD_PARAM + "originale-1.zip"));
    }

    @Test
    void signierteLinksGeltenHoechstensFuenfMinuten() throws Exception {
        getWithSession(PHOTOS_PATH + "0001/thumb").andExpect(status().isFound());

        assertThat(storage.lastValidity()).isLessThanOrEqualTo(GalleryLinkService.LINK_VALIDITY);
    }

    @Test
    void unbekanntesFotoLiefert404() throws Exception {
        getWithSession(PHOTOS_PATH + "9999/thumb").andExpect(status().isNotFound());
    }

    @Test
    void unbekannteVarianteLiefert404() throws Exception {
        getWithSession(PHOTOS_PATH + "0001/riesig").andExpect(status().isNotFound());
    }

    @Test
    void unbekanntesPaketLiefert404() throws Exception {
        getWithSession(ZIPS_PATH + "gibt-es-nicht.zip").andExpect(status().isNotFound());
    }

    @Test
    void pfadAngriffLiefert404() throws Exception {
        // Spring Security weist kodierte Schraegstriche schon mit 400 ab, sonst greift unser 404
        for (String attack : new String[]{PHOTOS_PATH + "..%2Fgeheim/original", ZIPS_PATH + "..%2Findex.json",
                PHOTOS_PATH + "..%5Cgeheim/original"}) {
            int status = getWithSession(attack).andReturn().getResponse().getStatus();
            assertThat(status).isIn(400, 404);
        }
    }

    @Test
    void linksOhneAnmeldungLiefern401() throws Exception {
        mockMvc.perform(get(PHOTOS_PATH + "0001/thumb")).andExpect(status().isUnauthorized());
        mockMvc.perform(get(ZIPS_PATH + "handy.zip")).andExpect(status().isUnauthorized());
    }
}
