package com.student.portfolio.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.student.portfolio.service.impl.GoogleAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/login/google")
@Tag(name = "Google OAuth2", description = "Google OAuth2 authentication API")
public class GoogleAuthController {

    @Value("${google.client.id}")
    private String clientId;

    private String redirectUri = "https://create-portfolios.netlify.app/login/google/callback";

    private final GoogleAuthService googleAuthService;

    public GoogleAuthController(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    @Operation(summary = "Redirect to Google", description = "Redirects user to Google's OAuth2 consent screen")
    @GetMapping
    public RedirectView redirectToGoogle() {
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=openid%20email%20profile";
        return new RedirectView(googleAuthUrl);
    }

    @Operation(summary = "Google OAuth2 callback", description = "Handles the callback from Google OAuth2 with authorization code")
    @PostMapping("/callback")
    public ResponseEntity<?> handleGoogleCallback(@RequestParam("code") String code) {
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body("Authorization code is required");
        }

        return googleAuthService.authenticateWithGoogle(code);
    }

}
