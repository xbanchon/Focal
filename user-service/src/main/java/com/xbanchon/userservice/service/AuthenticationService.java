package com.xbanchon.userservice.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.xbanchon.userservice.entity.UserAccount;
import com.xbanchon.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${google.client-id}")
    private String googleClientId;

    @Transactional
    public String authenticateWithGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String googleUserId = payload.getSubject();

            UserAccount user = userRepository.findByEmail(email)
                    .map(existingUser -> {
                        if (existingUser.getProviderId() == null) {
                            existingUser.setProviderId(googleUserId);
                            existingUser.setAuthProvider(UserAccount.AuthProvider.GOOGLE);
                        }
                        return existingUser;
                    })
                    .orElseGet(() -> {
                        log.info("Registering new user via Google: {}", email);
                        return userRepository.save(UserAccount.builder()
                                .email(email)
                                .authProvider(UserAccount.AuthProvider.GOOGLE)
                                .providerId(googleUserId)
                                .role(UserAccount.Role.USER)
                                .build());
                    });

            return jwtService.generateToken(user);
        } catch (Exception e) {
            log.error("Google authentication failed", e);
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }
}
