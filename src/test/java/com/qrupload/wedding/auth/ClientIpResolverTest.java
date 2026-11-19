package com.qrupload.wedding.auth;

import com.qrupload.wedding.service.ClientIpResolver;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClientIpResolverTest {

    private static MockHttpServletRequest requestWithHeaders(String... forwardedFor) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.4");
        for (String header : forwardedFor) {
            request.addHeader("X-Forwarded-For", header);
        }
        return request;
    }

    @Test
    void ohneForwardedForGiltDieVerbindungsadresse() {
        assertThat(new ClientIpResolver(1).resolve(requestWithHeaders())).isEqualTo("192.0.2.4");
    }

    @Test
    void einVertrauterProxyNimmtDenLetztenEintragOhneLeerzeichen() {
        assertThat(new ClientIpResolver(1).resolve(requestWithHeaders("6.6.6.6 , 198.51.100.1 ")))
                .isEqualTo("198.51.100.1");
    }

    @Test
    void zweiVertrauteProxysNehmenDenVorletztenEintrag() {
        assertThat(new ClientIpResolver(2).resolve(requestWithHeaders("198.51.100.1, 203.0.113.5, 10.0.0.1")))
                .isEqualTo("203.0.113.5");
    }

    @Test
    void headerKuerzerAlsProxyKetteGiltDieVerbindungsadresse() {
        assertThat(new ClientIpResolver(2).resolve(requestWithHeaders("6.6.6.6"))).isEqualTo("192.0.2.4");
    }

    @Test
    void mehrereHeaderZeilenWerdenVerbunden() {
        assertThat(new ClientIpResolver(2).resolve(requestWithHeaders("6.6.6.6", "203.0.113.5, 10.0.0.1")))
                .isEqualTo("203.0.113.5");
    }

    @Test
    void leereEintraegeZaehlenNicht() {
        assertThat(new ClientIpResolver(1).resolve(requestWithHeaders("198.51.100.1, , "))).isEqualTo("198.51.100.1");
    }

    @Test
    void wenigerAlsEinVertrauterProxyIstUngueltig() {
        assertThatThrownBy(() -> new ClientIpResolver(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
