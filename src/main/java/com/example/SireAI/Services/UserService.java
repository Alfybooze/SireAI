package com.example.SireAI.Services;

import com.example.SireAI.Controller.UserController;
import com.example.SireAI.Models.User;
import com.example.SireAI.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // Temporary storage for registration data (replace with Redis in production)
    private final Map<String, UserController.RegistrationRequest> tempRegistrationData = new ConcurrentHashMap<>();
    private static final int TEMP_DATA_EXPIRY_MINUTES = 30;

    /**
     * Store temporary registration data
     */
    public void storeTemporaryRegistration(UserController.RegistrationRequest request) {
        tempRegistrationData.put(request.getEmail(), request);
        log.info("Stored temporary registration data for email: {}", request.getEmail());
        
        // Schedule cleanup after expiry
        scheduleTempDataCleanup(request.getEmail());
    }

    /**
     * Get temporary registration data
     */
    public UserController.RegistrationRequest getTemporaryRegistration(String email) {
        return tempRegistrationData.get(email);
    }

    /**
     * Remove temporary registration data
     */
    public void removeTemporaryRegistration(String email) {
        tempRegistrationData.remove(email);
        log.info("Removed temporary registration data for email: {}", email);
    }

    /**
     * Create user with Telegram ID as primary key
     */
    public User createUser(String telegramUserId, String username, String email, String phoneNumber, String password) {
        try {
            // Hash password in production (use BCrypt or similar)
            String hashedPassword = password; // TODO: Implement password hashing
            
            User user = User.builder()
                    .telegramUserId(telegramUserId)
                    .username(username)
                    .email(email)
                    .phoneNumber(phoneNumber)
                    .password(hashedPassword)
                    .emailVerified(true) // Set to true since we verified via email
                    .isPhoneVerified(true) // Set to true since we verified via Telegram
                    .joinedAt(LocalDateTime.now())
                    .build();

            User savedUser = userRepository.save(user);
            log.info("User created successfully: {} (Telegram ID: {})", username, telegramUserId);
            
            return savedUser;
            
        } catch (Exception e) {
            log.error("Failed to create user: {} (Telegram ID: {})", username, telegramUserId, e);
            throw new RuntimeException("Failed to create user", e);
        }
    }

    /**
     * Check if user exists by phone number
     */
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).isPresent();
    }

    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     * Check if user exists by username
     */
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * Check if user exists by Telegram user ID
     */
    public boolean existsByTelegramUserId(String telegramUserId) {
        return userRepository.findByTelegramUserId(telegramUserId).isPresent();
    }

    /**
     * Find user by Telegram user ID
     */
    public User findByTelegramUserId(String telegramUserId) {
        return userRepository.findByTelegramUserId(telegramUserId).orElse(null);
    }

    /**
     * Find user by phone number
     */
    public User findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    /**
     * Find user by email
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Find user by username
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Update user profile
     */
    public User updateUser(String telegramUserId, User updatedUser) {
        User existingUser = findByTelegramUserId(telegramUserId);
        if (existingUser == null) {
            throw new RuntimeException("User not found with Telegram ID: " + telegramUserId);
        }

        // Update allowed fields
        if (updatedUser.getUsername() != null) {
            existingUser.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getFirstName() != null) {
            existingUser.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        }

        User savedUser = userRepository.save(existingUser);
        log.info("User updated: {} (Telegram ID: {})", savedUser.getUsername(), telegramUserId);
        
        return savedUser;
    }
    public User createUserWithEmail(String fullName, String username, String email, String password, String phoneNumber) {
    try {
        String hashedPassword = passwordEncoder.encode(password); // Use BCrypt encoding
        
        User user = User.builder()
                .username(username)
                .email(email)
                .password(hashedPassword)
                .emailVerified(true)
                .joinedAt(LocalDateTime.now())
                .build();

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFirstName(fullName.trim());
        }
        
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            user.setPhoneNumber(phoneNumber);
            user.setIsPhoneVerified(false);
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully via email: {} (ID: {})", username, savedUser.getUserId());
        
        return savedUser;
        
    } catch (Exception e) {
        log.error("Failed to create user via email: {}", username, e);
        throw new RuntimeException("Failed to create user", e);
    }
    }

    public Long deleteUser(Long userId) {
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
        throw new RuntimeException("User not found with ID: " + userId);
    }

    userRepository.deleteById(userId);
    log.info("User deleted: {} (ID: {})", user.getUsername(), userId);
    return userId;
    }

    /**
     * Schedule cleanup of temporary registration data
     */
    private void scheduleTempDataCleanup(String email) {
        new Thread(() -> {
            try {
                Thread.sleep(TEMP_DATA_EXPIRY_MINUTES * 60 * 1000);
                tempRegistrationData.remove(email);
                log.debug("Cleaned up expired temp registration data for email: {}", email);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
     public User linkTelegramToUser(String email, String telegramUserId, String phoneNumber) {
        try {
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            
            // Check if Telegram ID is already linked to another account
            if (existsByTelegramUserId(telegramUserId)) {
                throw new RuntimeException("This Telegram account is already linked to another user");
            }
            
            // Update user with Telegram information
            user.setTelegramUserId(telegramUserId);
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                user.setPhoneNumber(phoneNumber);
                user.setIsPhoneVerified(true);
            }
            
            User updatedUser = userRepository.save(user);
            log.info("Telegram linked successfully to user: {} (Telegram ID: {})", user.getUsername(), telegramUserId);
            
            return updatedUser;
            
        } catch (Exception e) {
            log.error("Failed to link Telegram to user: {}", email, e);
            throw new RuntimeException("Failed to link Telegram account", e);
        }
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("USER")
                .build();
    }
}
