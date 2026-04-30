package com.example.SireAI.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SireAI.Models.User;
import com.example.SireAI.Security.JwtUtils;
import com.example.SireAI.Services.EmailVerificationService;
import com.example.SireAI.Services.TelegramBotService;
import com.example.SireAI.Services.UserService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final TelegramBotService telegramBotService;
    private final EmailVerificationService emailVerificationService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Data
    public static class RegistrationRequest {
        private String fullName;
        private String phoneNumber;
        private String password;
        private String username;
        private String email;
    }

    @Data
    public static class EmailVerificationRequest {
        private String email;
        private String otp;
    }
     @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
 
    @Data
    public static class LoginResponse {
        private String token;
        private String username;
 
        public LoginResponse(String token, String username) {
            this.token = token;
            this.username = username;
        }
    }
    @Data
    public static class TelegramLinkRequest {
        private String phoneNumber;
        private String telegramUserId;
    }

    @Data
    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
    }

    /**
    * Step 1: User registers with name, email, username, password (phone optional)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request) {
        try {
            log.info("Registration attempt for USer with email: {}", request.getEmail());
            
            // Validate input
            if (request.getEmail() == null || request.getUsername() == null || 
                request.getPassword() == null || request.getFullName() == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Name, email, username, and password are required"));
            }

            // Check if user already exists
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Email already registered"));
            }

            if (userService.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Username already taken"));
            }

            // Send email verification token
            boolean emailSent = emailVerificationService.sendVerificationEmailWithData(
                request.getEmail(),
                request.getFullName(),
                request.getUsername(),
                request.getPassword(),
                request.getPhoneNumber()
            );

            if (!emailSent) {
                return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to send verification email"));
            }

            // Store temporary registration data keyed by email (in memory or Redis)
            userService.storeTemporaryRegistration(request);

            return ResponseEntity.ok(new ApiResponse(true, 
                "Verification email sent! Please check your email and enter the verification code to complete registration."));

        } catch (Exception e) {
            log.error("Registration error", e);
            return ResponseEntity.internalServerError()
                .body(new ApiResponse(false, "Registration failed"));
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
             String jwt = jwtUtils.generateToken(userDetails);
            
            User user = userService.findByUsername(request.getUsername());
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("type", "Bearer");
            response.put("userId", user.getUserId());
            response.put("email", user.getEmail());
            response.put("username", user.getUsername());
            
            return ResponseEntity.ok(new ApiResponse(true, "Login successful", response));
        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid credentials"));
        }
    }
    /**
     * Step 2: Verify email otp - create account immediately
     */
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody EmailVerificationRequest request) {
        try {
            log.info("Email verification attempt for: {}", request.getEmail());
            
            // Verify email otp
            boolean isValid = emailVerificationService.verifyEmailOTP(
                request.getEmail(), 
                request.getOtp()
            );

            if (!isValid) {
                return ResponseEntity.badRequest()
                     .body(new ApiResponse(false, "Invalid or expired OTP"));
            }

             // Get stored registration data
            RegistrationRequest tempData = userService.getTemporaryRegistration(request.getEmail());
            if (tempData == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Registration data not found or expired"));
            }

            // Create user account (phone number is optional)
            User user = userService.createUserWithEmail(
                tempData.getFullName(),
                tempData.getUsername(),
                tempData.getEmail(),
                tempData.getPassword(),
                tempData.getPhoneNumber() // Can be null
            );
 
            // Clean up temporary data
            userService.removeTemporaryRegistration(request.getEmail());
 
            log.info("User created successfully: {} (ID: {})", user.getUsername(), user.getUserId());
            
            // Check if phone number was provided for optional Telegram linking
            if (tempData.getPhoneNumber() != null) {
                String telegramMessage = String.format("""
                    🎉 Account created successfully! Welcome to SireAI, %s!
                    
                    📱 Optional: Link Telegram for seamless integration
                    
                    1. Message our bot: @SireAIBot
                    2. Send: /link %s
                    3. Return here with your Telegram ID
                    
                    Skip this step if you don't use Telegram.
                    """, user.getUsername(), tempData.getPhoneNumber());
                
                return ResponseEntity.ok(new ApiResponse(true, telegramMessage));
            } else {
                return ResponseEntity.ok(new ApiResponse(true, 
                    "🎉 Account created successfully! Welcome to SireAI, " + user.getUsername() + "!"));
            }

        } catch (Exception e) {
            log.error("Email verification error", e);
            return ResponseEntity.internalServerError()
                .body(new ApiResponse(false, "Verification failed"));
        }
    }

    /**
     * Step 3: Link Telegram account after user messages bot
     */
    @PostMapping("/link-telegram")
    public ResponseEntity<?> linkTelegram(@RequestBody TelegramLinkRequest request) {
        try {
            log.info("Telegram linking attempt for phone: {}", request.getPhoneNumber());
            
            // Validate Telegram ID format
            if (!request.getTelegramUserId().matches("\\d+")) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid Telegram ID format"));
            }

            // Get temporary registration data
            RegistrationRequest tempData = userService.getTemporaryRegistration(request.getPhoneNumber());
            if (tempData == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Registration data not found or expired"));
            }

            // Check if Telegram ID is already linked to another account
            if (userService.existsByTelegramUserId(request.getTelegramUserId())) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "This Telegram account is already linked to another user"));
            }

            // Verify that this Telegram ID has messaged our bot
            if (!telegramBotService.hasUserMessagedBot(request.getTelegramUserId(), request.getPhoneNumber())) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, 
                        "Please message our bot @YourBotUsername first and use the /link command"));
            }

            // Create user account
            User user = userService.createUser(
                request.getTelegramUserId(),
                tempData.getUsername(),
                tempData.getEmail(),
                tempData.getPhoneNumber(),
                tempData.getPassword()
            );

            // Clean up temporary data
            userService.removeTemporaryRegistration(request.getPhoneNumber());

            log.info("User created successfully: {} (Telegram ID: {})", user.getUsername(), user.getTelegramUserId());
            
            return ResponseEntity.ok(new ApiResponse(true, 
                "🎉 Account created successfully! Welcome to SireAI, " + user.getUsername() + "!"));

        } catch (Exception e) {
            log.error("Telegram linking error", e);
            return ResponseEntity.internalServerError()
                .body(new ApiResponse(false, "Failed to link Telegram account"));
        }
    }

    /**
     * Check if a phone number is registered
     */
    @GetMapping("/check-phone/{phoneNumber}")
    public ResponseEntity<?> checkPhone(@PathVariable String phoneNumber) {
        boolean exists = userService.existsByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Check if a Telegram ID has messaged our bot
     */
    @GetMapping("/check-telegram/{telegramUserId}")
    public ResponseEntity<?> checkTelegram(@PathVariable String telegramUserId) {
        boolean hasMessaged = telegramBotService.hasUserMessagedBot(telegramUserId, null);
        return ResponseEntity.ok(Map.of("hasMessagedBot", hasMessaged));
    }
}