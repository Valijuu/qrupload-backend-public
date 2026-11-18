package com.qrupload.wedding.filter;

import com.qrupload.wedding.service.JwtService;
import com.qrupload.wedding.service.SessionCookieFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter { // Filter nur einmal pro Request

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = sessionToken(request);

        if (token != null) {
            if (jwtService.isValid(token)) {
                String subject = jwtService.getSubject(token);

                UsernamePasswordAuthenticationToken auth = //Request kommt von einem authentifizierten Nutzer
                        new UsernamePasswordAuthenticationToken(subject, null, Collections.emptyList()); // 1. Para -> Wer, 2. Para -> Passwort, 3. Para -> Rollen
                SecurityContextHolder.getContext().setAuthentication(auth); // speichert die authentifizierung, ab jetzt sagt spring Security: "Der Nutzer ist eingeloggt"
            }
        }

        filterChain.doFilter(request, response); // request weiter durch die Filte-Kette -> zum Controller
    }

    private String sessionToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> SessionCookieFactory.COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
