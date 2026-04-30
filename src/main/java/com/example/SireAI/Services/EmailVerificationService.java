package com.example.SireAI.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    // In-memory storage for email verification tokens (replace with Redis in production)
    private final Map<String, EmailVerificationData> verificationTokens = new ConcurrentHashMap<>();
    
    private static final int TOKEN_LENGTH = 6;
    private static final int TOKEN_EXPIRY_MINUTES = 15;

    @lombok.Data
    private static class EmailVerificationData {
        private final String token;
        private final LocalDateTime expiresAt;
        private final String phoneNumber;
        private final String username;
        private final String fullName;
        private final String password;
    }

    /**
     * Generate and send verification email
     * Note: This is a placeholder implementation. In production, integrate with actual email service
     */
    public boolean sendVerificationEmail(String email, String phoneNumber, String username) {
        try {
            // Generate 6-digit token
            String token = generateToken();
            
            // Store token with expiry
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES);
            verificationTokens.put(email, new EmailVerificationData(token, expiresAt, phoneNumber, username, null, null));
            
            // Log the verification details (in production, send actual email)
            log.info("=== EMAIL VERIFICATION TOKEN ===");
            log.info("To: {}", email);
            log.info("Phone: {}", phoneNumber);
            log.info("Username: {}", username);
            log.info("Verification Code: {}", token);
            log.info("Expires at: {}", expiresAt);
            log.info("================================");
            
            // TODO: Integrate with actual email service (SendGrid, AWS SES, etc.)
            // For now, we'll simulate success
            log.info("Verification email sent to {} for phone {}", email, phoneNumber);
            
            // Schedule cleanup after expiry
            scheduleTokenCleanup(email);
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send verification email to {}", email, e);
            return false;
        }
    }

    /**
     * Verify email token
     */
    public boolean verifyEmailToken(String email, String phoneNumber, String token) {
        EmailVerificationData data = verificationTokens.get(email);
        
        if (data == null) {
            log.warn("No verification token found for email: {}", email);
            return false;
        }
        
        // Check if token has expired
        if (LocalDateTime.now().isAfter(data.getExpiresAt())) {
            log.warn("Verification token expired for email: {}", email);
            verificationTokens.remove(email);
            return false;
        }
        
        // Check if phone number matches
        if (!data.getPhoneNumber().equals(phoneNumber)) {
            log.warn("Phone number mismatch for email: {}", email);
            return false;
        }
        
        // Check if token matches
        if (!data.getToken().equals(token)) {
            log.warn("Invalid verification token for email: {}", email);
            return false;
        }
        
        // Token is valid - remove it
        verificationTokens.remove(email);
        log.info("Email verification successful for: {}", email);
        return true;
    }

    /**
     * Generate random 6-digit token
     */
    private String generateToken() {
        Random random = new Random();
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(random.nextInt(10));
        }
        return token.toString();
    }

     public boolean verifyEmailOTP(String email, String otp) {
        EmailVerificationData data = verificationTokens.get(email);
        
        if (data == null) {
            log.warn("No verification token found for email: {}", email);
            return false;
        }
        
        // Check if token has expired
        if (LocalDateTime.now().isAfter(data.getExpiresAt())) {
            log.warn("Verification token expired for email: {}", email);
            verificationTokens.remove(email);
            return false;
        }
        
        // Check if token matches (OTP)
        if (!data.getToken().equals(otp)) {
            log.warn("Invalid verification OTP for email: {}", email);
            return false;
        }
        
        // OTP is valid - remove it
        verificationTokens.remove(email);
        log.info("Email OTP verification successful for: {}", email);
        return true;
    }

    /**
     * Schedule token cleanup after expiry
     */
    private void scheduleTokenCleanup(String email) {
        // In production, use a scheduled executor or Redis TTL
        new Thread(() -> {
            try {
                Thread.sleep(TOKEN_EXPIRY_MINUTES * 60 * 1000); // Wait for expiry
                verificationTokens.remove(email);
                log.debug("Cleaned up expired token for email: {}", email);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Check if email has pending verification
     */
    public boolean hasPendingVerification(String email) {
        EmailVerificationData data = verificationTokens.get(email);
        if (data == null) return false;
        
        // Check if expired
        if (LocalDateTime.now().isAfter(data.getExpiresAt())) {
            verificationTokens.remove(email);
            return false;
        }
        
        return true;
    }

    /**
     * Get verification token for testing purposes
     */
    public String getVerificationToken(String email) {
        EmailVerificationData data = verificationTokens.get(email);
        return data != null ? data.getToken() : null;
    }
     public boolean sendVerificationEmailWithData(String email, String fullName, String username, String password, String phoneNumber) {
        try {
            // Generate 6-digit token
            String token = generateToken();
            
            // Store token with expiry and full registration data
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES);
            verificationTokens.put(email, new EmailVerificationData(token, expiresAt, phoneNumber, username, fullName, password));
            
            // Log the verification details (in production, send actual email)
            log.info("=== EMAIL VERIFICATION TOKEN ===");
            log.info("To: {}", email);
            log.info("Full Name: {}", fullName);
            log.info("Username: {}", username);
            log.info("Phone: {}", phoneNumber);
            log.info("Verification Code: {}", token);
            log.info("Expires at: {}", expiresAt);
            log.info("================================");
            
            // TODO: Integrate with actual email service (SendGrid, AWS SES, etc.)
            // For now, we'll simulate success
            log.info("Verification email sent to {} for registration", email);
            
            // Schedule cleanup after expiry
            scheduleTokenCleanup(email);
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send verification email to {}", email, e);
            return false;
        }
    }
}