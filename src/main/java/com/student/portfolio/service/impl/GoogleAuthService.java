package com.student.portfolio.service.impl;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.student.portfolio.entity.User;
import com.student.portfolio.repository.UserRepository;
import com.student.portfolio.security.JwtUtil;

@Service
public class GoogleAuthService {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    public GoogleAuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                           JwtUtil jwtUtil, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<?> authenticateWithGoogle(String code) {
        // Exchange authorization code for access token
        String accessToken = exchangeCodeForToken(code);
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to exchange code for token");
        }

        // Get user info from Google
        Map<String, Object> userInfo = getUserInfo(accessToken);
        if (userInfo == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get user info from Google");
        }

        // Extract email from user info
        String email = (String) userInfo.get("email");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email not provided by Google");
        }

        // Check if user exists
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            // Create new user with Google info
            user = createUserFromGoogleInfo(userInfo);
            userRepository.save(user);
        }

        // Generate JWT tokens
        String token = jwtUtil.generateToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        return ResponseEntity.ok(Map.of(
            "accessToken", token,
            "refreshToken", refreshToken,
            "user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail()
            )
        ));
    }

    private String exchangeCodeForToken(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            }
        } catch (Exception e) {
            // Log error
        }
        return null;
    }

    private Map<String, Object> getUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (Exception e) {
            // Log error
        }
        return null;
    }

    private User createUserFromGoogleInfo(Map<String, Object> userInfo) {
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String givenName = (String) userInfo.get("given_name");
        String familyName = (String) userInfo.get("family_name");
        
        // Generate a unique username
        String baseUsername = givenName != null ? givenName.toLowerCase() : 
                            name != null ? name.toLowerCase().replace(" ", "") : 
                            email.split("@")[0];
        
        String username = generateUniqueUsername(baseUsername);
        
        // Generate a random password
        String password = UUID.randomUUID().toString();
        
        return User.builder()
                .email(email)
                .username(username)
                .password(passwordEncoder.encode(password))
                .emailVerified(true)
                .build();
    }

    private String generateUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
}