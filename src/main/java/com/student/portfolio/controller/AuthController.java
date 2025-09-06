package com.student.portfolio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.student.portfolio.security.JwtUtil;
import com.student.portfolio.service.impl.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
@Validated
@Tag(name = "Authentication", description = "Authentication API for user registration and login")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService , JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Start registration", description = "Initiate user registration by providing email. An OTP will be sent to the email.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
        @ApiResponse(responseCode = "400", description = "Email already registered")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Parameter(description = "User's email address", required = true)
            @RequestParam(name = "email") @NotBlank String email) {
        authService.startRegistration(email);
        return ResponseEntity.ok("OTP sent to email");
    }

    @Operation(summary = "Verify OTP", description = "Verify the OTP sent to the user's email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @Parameter(description = "User's email address", required = true ,name = "email")
            @RequestParam(name = "email") @NotBlank String email,
            
            @Parameter(description = "OTP received via email", required = true , name = "otp")
            @RequestParam(name = "otp") @NotBlank String otp) {
        authService.verifyOtp(email, otp);
        return ResponseEntity.ok("Email verified. Now set username & password.");
    }

    @Operation(summary = "Set credentials", description = "Set username and password after email verification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Credentials set successfully"),
        @ApiResponse(responseCode = "400", description = "Email not verified or username already taken")
    })
    @PostMapping("/set-creds")
    public ResponseEntity<?> setCreds(@RequestBody SetCredsRequest req) {
        authService.setUsernameAndPassword(req.email(), req.username(), req.password());
        return ResponseEntity.ok("Credentials set. You can login now.");
    }

    @Operation(summary = "User login", description = "Authenticate user with username and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "400", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        var resp = authService.login(req.username(), req.password());
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Refresh token", description = "Get a new access token using a valid refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @Parameter(description = "Valid refresh token", required = true , name = "refreshToken")
            @RequestParam(name = "refreshToken") String refreshToken) {
        var resp = authService.refresh(refreshToken);
        return ResponseEntity.ok(resp);
    }
    @PostMapping("/check-token")
    public ResponseEntity<Boolean> checkToken(@RequestBody String token) {
        boolean valid = jwtUtil.validate(token);
        return ResponseEntity.ok(valid);
    }

    // Request record classes
    record SetCredsRequest(
            @NotBlank String email, 
            @NotBlank String username, 
            @NotBlank String password) {}
    
    record LoginRequest(@NotBlank String username, @NotBlank String password) {}
}