package com.xbanchon.userservice.controller;

import com.xbanchon.userservice.dto.AuthResponse;
import com.xbanchon.userservice.dto.GoogleLoginRequest;
import com.xbanchon.userservice.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleLoginRequest req) {
        String internalJwt = authenticationService.authenticateWithGoogle(req.token());

        return ResponseEntity.ok(new AuthResponse(internalJwt));
    }
}
