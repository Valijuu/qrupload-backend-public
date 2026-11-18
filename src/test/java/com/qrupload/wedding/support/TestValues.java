package com.qrupload.wedding.support;

import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public final class TestValues {

    public static final String JWT_SECRET = "0123456789012345678901234567890123456789";
    public static final String PASSWORD = "test-passwort";
    public static final String SESSION_COOKIE = "__session";
    public static final String LOGIN_PATH = "/api/login";
    public static final String PROTECTED_PATH = "/api/gallery";
    public static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    public static final String WRONG_PASSWORD = "falsch";
    public static final String GUEST_SUBJECT = "Hochzeitsgast";

    private static final int IPS_PER_BLOCK = 250;
    private static final AtomicInteger NEXT_IP = new AtomicInteger();

    private TestValues() {
    }

    /** Frische Client-IP je Aufruf, damit sich Tests nicht gegenseitig das Rate-Limit fuellen. */
    public static String uniqueIp() {
        int n = NEXT_IP.getAndIncrement();
        return "10.9." + (n / IPS_PER_BLOCK) + "." + (n % IPS_PER_BLOCK);
    }

    /** Meldet sich mit dem Testpasswort an und liefert das Session-Cookie fuer Folgeanfragen. */
    public static Cookie loginSessionCookie(MockMvc mockMvc, String clientIp) throws Exception {
        String setCookie = mockMvc.perform(loginRequest(clientIp, PASSWORD))
                .andReturn().getResponse().getHeader(HttpHeaders.SET_COOKIE);
        String jwt = setCookie.substring((SESSION_COOKIE + "=").length(), setCookie.indexOf(';'));
        return new Cookie(SESSION_COOKIE, jwt);
    }

    public static String loginBody(String password) {
        return "{\"password\":\"" + password + "\"}";
    }

    /** Login-Anfrage, die ueber X-Forwarded-For als Client mit der gegebenen IP auftritt. */
    public static MockHttpServletRequestBuilder loginRequest(String clientIp, String password) {
        return post(LOGIN_PATH).header(FORWARDED_FOR_HEADER, clientIp)
                .contentType(MediaType.APPLICATION_JSON).content(loginBody(password));
    }
}
