package com.qrupload.wedding.auth;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

final class TestValues {

    static final String JWT_SECRET = "0123456789012345678901234567890123456789";
    static final String PASSWORD = "test-passwort";
    static final String SESSION_COOKIE = "__session";
    static final String LOGIN_PATH = "/api/login";
    static final String PROTECTED_PATH = "/api/photos";
    static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    static final String WRONG_PASSWORD = "falsch";
    static final String GUEST_SUBJECT = "Hochzeitsgast";

    private TestValues() {
    }

    static String loginBody(String password) {
        return "{\"password\":\"" + password + "\"}";
    }

    /** Login-Anfrage, die ueber X-Forwarded-For als Client mit der gegebenen IP auftritt. */
    static MockHttpServletRequestBuilder loginRequest(String clientIp, String password) {
        return post(LOGIN_PATH).header(FORWARDED_FOR_HEADER, clientIp)
                .contentType(MediaType.APPLICATION_JSON).content(loginBody(password));
    }
}
