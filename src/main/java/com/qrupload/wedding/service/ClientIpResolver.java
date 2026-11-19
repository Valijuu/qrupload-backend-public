package com.qrupload.wedding.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bestimmt die Client-IP fuer das Login-Limit. Jeder Proxy haengt die Adresse an, von der er die
 * Anfrage bekam. Nur die rechten Eintraege stammen daher von unseren Proxys, alles links davon
 * kann der Client selbst setzen und wird ignoriert.
 */
@Component
public class ClientIpResolver {

    static final String TRUSTED_PROXY_HOPS_PROPERTY = "gallery.trusted-proxy-hops";
    static final String INVALID_HOPS_MESSAGE = TRUSTED_PROXY_HOPS_PROPERTY + " muss mindestens 1 sein";

    private static final String FORWARDED_FOR = "X-Forwarded-For";
    private static final String SEPARATOR = ",";

    private final int trustedProxyHops;

    public ClientIpResolver(@Value("${" + TRUSTED_PROXY_HOPS_PROPERTY + ":1}") int trustedProxyHops) {
        if (trustedProxyHops < 1) {
            throw new IllegalArgumentException(INVALID_HOPS_MESSAGE);
        }
        this.trustedProxyHops = trustedProxyHops;
    }

    /** Der Eintrag, den der aeusserste vertraute Proxy angehaengt hat, sonst die Verbindungsadresse. */
    public String resolve(HttpServletRequest request) {
        List<String> entries = forwardedForEntries(request);
        if (entries.size() < trustedProxyHops) {
            // Die Anfrage kam nicht ueber die erwartete Kette, der Header ist dann nicht vertrauenswuerdig
            return request.getRemoteAddr();
        }
        return entries.get(entries.size() - trustedProxyHops);
    }

    private static List<String> forwardedForEntries(HttpServletRequest request) {
        List<String> entries = new ArrayList<>();
        for (String header : Collections.list(request.getHeaders(FORWARDED_FOR))) {
            for (String entry : header.split(SEPARATOR)) {
                if (!entry.isBlank()) {
                    entries.add(entry.trim());
                }
            }
        }
        return entries;
    }
}
