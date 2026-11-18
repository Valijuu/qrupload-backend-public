package com.qrupload.wedding.auth;

import com.qrupload.wedding.support.GalleryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.qrupload.wedding.support.TestValues.PASSWORD;
import static com.qrupload.wedding.support.TestValues.loginRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@GalleryTest
class LegacyAuthRemovedTest {

    private static final String LEGACY_VALIDATE_PATH = "/api/auth/validate";
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final int HTTP_NOT_FOUND = 404;
    private static final String ANY_TOKEN_BODY = "{\"token\":\"egal\"}";
    private static final String FOREIGN_ORIGIN = "https://fremd.example";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void alterValidateEndpunktIstWeg() throws Exception {
        int status = mockMvc.perform(post(LEGACY_VALIDATE_PATH)
                        .contentType(MediaType.APPLICATION_JSON).content(ANY_TOKEN_BODY))
                .andReturn().getResponse().getStatus();
        assertThat(status).isIn(HTTP_UNAUTHORIZED, HTTP_NOT_FOUND);
    }

    @Test
    void keineCorsFreigabeFuerFremdeOrigins() throws Exception {
        String allowOrigin = mockMvc.perform(loginRequest("10.3.0.1", PASSWORD)
                        .header(HttpHeaders.ORIGIN, FOREIGN_ORIGIN))
                .andReturn().getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        assertThat(allowOrigin).isNull();
    }
}
