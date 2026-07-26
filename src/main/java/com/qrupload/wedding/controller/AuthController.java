package com.qrupload.wedding.controller;

import com.qrupload.wedding.dto.TokenRequest;
import com.qrupload.wedding.dto.TokenResponse;
import com.qrupload.wedding.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final String validToken;
    private final JwtService jwtService;

    public AuthController(JwtService jwtService, @Value("${app.access-token}") String validToken) {
        this.jwtService = jwtService;
        this.validToken = validToken;
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenResponse> validate(@RequestBody TokenRequest tokenRequest) {
        if (validToken.equals(tokenRequest.token())){

            String jwt = jwtService.generateToken("Hochzeitsgast");

            return ResponseEntity.ok(new TokenResponse(true, jwt));
        }
        return ResponseEntity.status(401).body(new TokenResponse(false, "Ungültiger Token"));
    }
}
