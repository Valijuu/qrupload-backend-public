package com.qrupload.wedding.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

@Service
public class JwtService {

    // Sitzung gilt wenige Stunden: lang genug fuer einen Galerie-Besuch, kurz genug bei geteiltem Passwort
    public static final Duration LIFETIME = Duration.ofHours(6);

    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes()); //mindestens 32 Zeichen sonst wird ein Fehler geworfen
    }

    public String generateToken(String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + LIFETIME.toMillis());

        return Jwts.builder()
                .subject(subject) //claim (sub) token besitzer
                .issuedAt(now) //wann wurde der Token erstellt
                .expiration(expiryDate) //wann läuft der Token aus
                .signWith(secretKey) //mit Geheimnis signieren, keiner kann Token fälschen
                .compact(); //baut das zusammen zu xxx.yyy.zzz
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token); //versucht zu parsen + Signatur prüfen
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
