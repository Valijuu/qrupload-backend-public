package com.qrupload.wedding.auth;

import com.qrupload.wedding.support.GalleryTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.util.Date;

import static com.qrupload.wedding.support.TestValues.JWT_SECRET;
import static com.qrupload.wedding.support.TestValues.GUEST_SUBJECT;
import static com.qrupload.wedding.support.TestValues.PASSWORD;
import static com.qrupload.wedding.support.TestValues.PROTECTED_PATH;
import static com.qrupload.wedding.support.TestValues.SESSION_COOKIE;
import static com.qrupload.wedding.support.TestValues.loginRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GalleryTest
class SessionCookieTest {

    private static final String LOGIN_IP = "10.2.0.1";
    private static final Duration MIN_LIFETIME = Duration.ofHours(1);
    private static final Duration MAX_LIFETIME = Duration.ofHours(12);
    private static final Duration ISSUED_BEFORE_EXPIRATION = Duration.ofSeconds(1);
    private static final Duration EXPIRED_SINCE = Duration.ofMinutes(1);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int TAMPERED_SUFFIX_LENGTH = 2;

    @Autowired
    private MockMvc mockMvc;

    private String loginSetCookie() throws Exception {
        MvcResult result = mockMvc.perform(loginRequest(LOGIN_IP, PASSWORD)).andReturn();
        return result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
    }

    private String jwtFromSetCookie(String setCookie) {
        return setCookie.substring((SESSION_COOKIE + "=").length(), setCookie.indexOf(';'));
    }

    private String tokenExpiring(Date expiration) {
        return Jwts.builder().subject(GUEST_SUBJECT)
                .issuedAt(new Date(expiration.getTime() - ISSUED_BEFORE_EXPIRATION.toMillis()))
                .expiration(expiration)
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes()))
                .compact();
    }

    @Test
    void cookieHatHttpOnlySecureSameSiteLaxUndPathRoot() throws Exception {
        assertThat(loginSetCookie())
                .contains("HttpOnly")
                .contains("Secure")
                .contains("SameSite=Lax")
                .contains("Path=/");
    }

    @Test
    void cookieLaeuftNachWenigenStundenAb() throws Exception {
        String setCookie = loginSetCookie();
        long maxAgeSeconds = Long.parseLong(setCookie.replaceAll(".*Max-Age=(\\d+).*", "$1"));
        assertThat(Duration.ofSeconds(maxAgeSeconds)).isBetween(MIN_LIFETIME, MAX_LIFETIME);
    }

    @Test
    void gueltigesCookieGibtZugriffAufGeschuetztenEndpunkt() throws Exception {
        String jwt = jwtFromSetCookie(loginSetCookie());
        mockMvc.perform(get(PROTECTED_PATH).cookie(new Cookie(SESSION_COOKIE, jwt)))
                .andExpect(status().isOk());
    }

    @Test
    void fehlendesCookieLiefert401() throws Exception {
        mockMvc.perform(get(PROTECTED_PATH)).andExpect(status().isUnauthorized());
    }

    @Test
    void abgelaufenesCookieLiefert401() throws Exception {
        String expired = tokenExpiring(new Date(System.currentTimeMillis() - EXPIRED_SINCE.toMillis()));
        mockMvc.perform(get(PROTECTED_PATH).cookie(new Cookie(SESSION_COOKIE, expired)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void manipuliertesCookieLiefert401() throws Exception {
        String jwt = jwtFromSetCookie(loginSetCookie());
        String tampered = jwt.substring(0, jwt.length() - TAMPERED_SUFFIX_LENGTH) + (jwt.endsWith("A") ? "BB" : "AA");
        mockMvc.perform(get(PROTECTED_PATH).cookie(new Cookie(SESSION_COOKIE, tampered)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authorizationHeaderAlleinReichtNichtAus() throws Exception {
        String jwt = jwtFromSetCookie(loginSetCookie());
        mockMvc.perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + jwt))
                .andExpect(status().isUnauthorized());
    }
}
