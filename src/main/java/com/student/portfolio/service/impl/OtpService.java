package com.student.portfolio.service.impl;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OtpService {
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    
    private record OtpEntry(String otp, Instant expiry, int attemptCount) {}
    private final Map<String, OtpEntry> otps = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private static final int MAX_ATTEMPTS = 3;
    private static final int OTP_EXPIRY_MINUTES = 5;

    public String generateOtp(String email) {
        // If OTP already exists for this email, check if it's expired
        OtpEntry existingEntry = otps.get(email);
        if (existingEntry != null) {
            if (existingEntry.expiry.isAfter(Instant.now())) {
                // OTP still valid, don't generate a new one
                log.info("OTP already exists for {} and is still valid", email);
                return existingEntry.otp;
            }
            // OTP expired, remove it
            otps.remove(email);
        }
        
        int code = 100000 + random.nextInt(900000);
        String otp = String.valueOf(code);
        Instant expiry = Instant.now().plusSeconds(OTP_EXPIRY_MINUTES * 60);
        otps.put(email, new OtpEntry(otp, expiry, 0));
        
        log.info("Generated OTP for {} -> {} (expires at: {})", email, otp, expiry);
        return otp;
    }

    public boolean verify(String email, String otp) {
        OtpEntry entry = otps.get(email);
        if (entry == null) {
            log.warn("No OTP found for email: {}", email);
            return false;
        }
        
        // Check if OTP is expired
        if (entry.expiry.isBefore(Instant.now())) {
            otps.remove(email);
            log.warn("OTP expired for email: {}", email);
            return false;
        }
        
        // Check if too many attempts
        if (entry.attemptCount >= MAX_ATTEMPTS) {
            otps.remove(email);
            log.warn("Too many OTP attempts for email: {}", email);
            return false;
        }
        
        // Update attempt count
        otps.put(email, new OtpEntry(entry.otp, entry.expiry, entry.attemptCount + 1));
        
        // Check if OTP matches
        boolean ok = entry.otp.equals(otp);
        if (ok) {
            otps.remove(email);
            log.info("OTP verified successfully for email: {}", email);
        } else {
            log.warn("Invalid OTP attempt for email: {}", email);
        }
        
        return ok;
    }
    
    public boolean hasActiveOtp(String email) {
        OtpEntry entry = otps.get(email);
        return entry != null && entry.expiry.isAfter(Instant.now());
    }
    
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredOtps() {
        Instant now = Instant.now();
        int count = 0;
        
        for (Map.Entry<String, OtpEntry> entry : otps.entrySet()) {
            if (entry.getValue().expiry.isBefore(now)) {
                otps.remove(entry.getKey());
                count++;
            }
        }
        
        if (count > 0) {
            log.info("Cleaned up {} expired OTPs", count);
        }
    }
}