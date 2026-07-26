package com.qrupload.wedding.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000L; //24 Stunden

    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes()); //mindestens 32 Zeichen sonst wird ein Fehler geworfen
    }

    public String generateToken(String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

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
