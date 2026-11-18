package com.qrupload.wedding.auth;

import com.qrupload.wedding.service.ClientIpResolver;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {

    private final ClientIpResolver resolver = new ClientIpResolver();

    @Test
    void ohneForwardedForGiltDieVerbindungsadresse() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.4");

        assertThat(resolver.resolve(request)).isEqualTo("192.0.2.4");
    }

    @Test
    void ersterForwardedForEintragOhneLeerzeichen() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", " 198.51.100.1 , 10.0.0.1");

        assertThat(resolver.resolve(request)).isEqualTo("198.51.100.1");
    }
}
