package com.qrupload.wedding.config;

import com.qrupload.wedding.filter.JwtAuthFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Keine Server-Session, die Sitzung steckt im JWT-Cookie __session
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login").permitAll()
                        // Fehlerseiten nicht als 401 tarnen: sonst haelt das Frontend jeden 500er fuer eine abgelaufene Sitzung
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                        ))
                        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Filter vor dem Standard-Login einfügen, erst JWT prüfen danach andere Auth-Mechanismen
        return http.build();
    }

    // Es gibt keine Benutzerkonten. Ohne diese Bean erzeugt Spring einen Standardbenutzer und schreibt sein Passwort ins Log.
    @Bean
    public UserDetailsService noUserAccounts() {
        return new InMemoryUserDetailsManager();
    }
}
