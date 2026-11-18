package com.qrupload.wedding.auth;

import com.qrupload.wedding.support.GalleryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.qrupload.wedding.support.TestValues.FORWARDED_FOR_HEADER;
import static com.qrupload.wedding.support.TestValues.LOGIN_PATH;
import static com.qrupload.wedding.support.TestValues.PASSWORD;
import static com.qrupload.wedding.support.TestValues.SESSION_COOKIE;
import static com.qrupload.wedding.support.TestValues.WRONG_PASSWORD;
import static com.qrupload.wedding.support.TestValues.loginRequest;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GalleryTest
class LoginControllerTest {

    private static final String MISSING_PASSWORD_IP = "10.1.0.3";
    private static final String EMPTY_JSON_BODY = "{}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void richtigesPasswortLiefert204UndSessionCookie() throws Exception {
        mockMvc.perform(loginRequest("10.1.0.1", PASSWORD))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, startsWith(SESSION_COOKIE + "=")));
    }

    @Test
    void falschesPasswortLiefert401OhneCookie() throws Exception {
        mockMvc.perform(loginRequest("10.1.0.2", WRONG_PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
                .andExpect(content().string(not(containsString(PASSWORD))));
    }

    @Test
    void fehlendesOderLeeresPasswortLiefert401() throws Exception {
        mockMvc.perform(post(LOGIN_PATH).header(FORWARDED_FOR_HEADER, MISSING_PASSWORD_IP)
                        .contentType(MediaType.APPLICATION_JSON).content(EMPTY_JSON_BODY))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(loginRequest(MISSING_PASSWORD_IP, ""))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post(LOGIN_PATH).header(FORWARDED_FOR_HEADER, MISSING_PASSWORD_IP))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passwortMitGleichemPraefixOderSuffixWirdAbgelehnt() throws Exception {
        for (String almost : new String[]{PASSWORD + "x", PASSWORD.substring(1), PASSWORD.toUpperCase()}) {
            mockMvc.perform(loginRequest("10.1.0.4", almost))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void kaputtesJsonLiefert400StattAbgelaufenerSitzung() throws Exception {
        // Unerwartete Fehler duerfen nicht als 401 ankommen, sonst haelt das Frontend die Sitzung fuer abgelaufen
        mockMvc.perform(post(LOGIN_PATH).header(FORWARDED_FOR_HEADER, "10.1.0.5")
                        .contentType(MediaType.APPLICATION_JSON).content("{kaputt"))
                .andExpect(status().isBadRequest());
    }
}
