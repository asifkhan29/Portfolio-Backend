package com.student.portfolio.service.impl;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.student.portfolio.entity.User;
import com.student.portfolio.exception.CustomException;
import com.student.portfolio.repository.UserRepository;
import com.student.portfolio.security.JwtUtil;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    // simple in-memory refresh token store: refreshToken -> username
    private final Map<String, String> refreshStore = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository, OtpService otpService, 
                      PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                      EmailService emailService) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;

    }

    public void startRegistration(String email) {
        // Check if email already exists and verified
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent() && existing.get().isEmailVerified()) {
            throw new CustomException("Email already registered and verified");
        }
        
        // If user exists but not verified, delete the old record
        if (existing.isPresent()) {
            userRepository.deleteByEmail(email);
            log.info("Deleted unverified user with email: {}", email);
        }
        
        // Generate OTP
        String otp = otpService.generateOtp(email);
        emailService.sendOtp(email, otp);
        log.info("Generated OTP for {} -> {}.", email, otp);
        // TODO: Send OTP via email provider in prod
    }

    public void verifyOtp(String email, String otp) {
        boolean ok = otpService.verify(email, otp);
        if (!ok) throw new CustomException("Invalid or expired OTP");
        
        // Create new user with emailVerified true
        User newUser = User.builder()
                .email(email)
                .emailVerified(true)
                .build();
        userRepository.save(newUser);
    }

    public void setUsernameAndPassword(String email, String username, String rawPassword) {
        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isEmpty() || !byEmail.get().isEmailVerified()) {
            throw new CustomException("Email not found or not verified");
        }
        
        // Check username unique
        if (userRepository.existsByUsername(username)) {
            throw new CustomException("Username already taken");
        }
        
        User u = byEmail.get();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(u);
    }

    public AuthResponse login(String username, String rawPassword) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("Invalid credentials"));
        
        if (!passwordEncoder.matches(rawPassword, u.getPassword())) {
            throw new CustomException("Invalid credentials");
        }
        
        String token = jwtUtil.generateToken(u.getUsername());
        String refresh = jwtUtil.generateRefreshToken(u.getUsername());
        refreshStore.put(refresh, u.getUsername());
        
        log.info("User {} logged in", username);
        return new AuthResponse(token, refresh);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtUtil.validate(refreshToken)) {
            throw new CustomException("Invalid refresh token");
        }
        
        String username = jwtUtil.getSubject(refreshToken);
        String stored = refreshStore.get(refreshToken);
        
        if (stored == null || !stored.equals(username)) {
            throw new CustomException("Refresh token not recognized");
        }
        
        String token = jwtUtil.generateToken(username);
        return new AuthResponse(token, refreshToken);
    }

    public static record AuthResponse(String accessToken, String refreshToken) {}
}