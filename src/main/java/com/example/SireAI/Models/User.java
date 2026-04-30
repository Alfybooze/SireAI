package com.example.SireAI.Models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "users", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "telegram_user_id", unique = true, nullable = true)
    private String telegramUserId;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "tier")
    @Builder.Default
    private String tier = "free";

    @Column(name = "message_count")
    @Builder.Default
    private Integer messageCount = 0;

    @Column(name = "tokens")
    @Builder.Default
    private Integer tokens = 0;

    @Column(name = "is_banned")
    @Builder.Default
    private Boolean isBanned = false;

    @Column(name = "joined_at")
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "daily_message_count")
    @Builder.Default
    private Integer dailyMessageCount = 0;

    @Column(name = "last_message_date")
    @Builder.Default
    private LocalDate lastMessageDate = LocalDate.now();

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "active_booking_data", columnDefinition = "jsonb")
    private Map<String, Object> activeBookingData;

    @Column(name = "trial_start_date")
    @Builder.Default
    private LocalDateTime trialStartDate = LocalDateTime.now();

    @Column(name = "trial_daily_paid_count")
    @Builder.Default
    private Integer trialDailyPaidCount = 0;

    @Column(name = "trial_daily_reset")
    @Builder.Default
    private LocalDate trialDailyReset = LocalDate.now();

    @Column(name = "second_booking")
    private String secondBooking;

    @Column(name = "lite_daily_count")
    @Builder.Default
    private Integer liteDailyCount = 0;

    @Column(name = "lite_premium_previews")
    @Builder.Default
    private Integer litePremiumPreviews = 0;

    @Column(name = "lite_daily_reset")
    @Builder.Default
    private LocalDate liteDailyReset = LocalDate.now();

    @Column(name = "current_plan")
    private String currentPlan;

    @Column(name = "lite_odds_used")
    @Builder.Default
    private Integer liteOddsUsed = 0;

    @Column(name = "lite_odds_reset")
    @Builder.Default
    private LocalDate liteOddsReset = LocalDate.now();

    @Column(name = "bot_role")
    private String botRole;

    @Column(name = "bot_preference")
    private String botPreference;

    @Column(name = "terms_accepted")
    @Builder.Default
    private Boolean termsAccepted = false;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;
 
    @Column(name = "email_verification_token")
    private String emailVerificationToken;
 
    @Column(name = "email_token_expires_at")
    private LocalDateTime emailTokenExpiresAt;

    @Column(name = "is_phone_verified")
    @Builder.Default
    private Boolean isPhoneVerified = false;

    @Column(name = "password")
    private String password;

    // Utility methods
    public boolean isTrialActive() {
        return trialStartDate != null && 
               trialStartDate.plusDays(7).isAfter(LocalDateTime.now());
    }

    public boolean hasExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean canSendMessage() {
        if (Boolean.TRUE.equals(isBanned)) return false;
        if (hasExpired()) return false;
        
        // Reset daily count if it's a new day
        if (!LocalDate.now().equals(lastMessageDate)) {
            dailyMessageCount = 0;
            lastMessageDate = LocalDate.now();
        }
        
        // Check tier-based limits
        switch (tier != null ? tier : "free") {
            case "premium":
                return true; // Unlimited for premium
            case "lite":
                return liteDailyCount < 10; // 10 messages per day for lite
            case "trial":
                return trialDailyPaidCount < 5; // 5 messages per day for trial
            default: // free
                return dailyMessageCount < 3; // 3 messages per day for free
        }
    }

    public void incrementMessageCount() {
        dailyMessageCount++;
        messageCount++;
        
        // Update tier-specific counters
        if ("trial".equals(tier)) {
            trialDailyPaidCount++;
        } else if ("lite".equals(tier)) {
            liteDailyCount++;
        }
    }

    public void resetDailyCounters() {
        dailyMessageCount = 0;
        liteDailyCount = 0;
        trialDailyPaidCount = 0;
        liteOddsUsed = 0;
        lastMessageDate = LocalDate.now();
        liteDailyReset = LocalDate.now();
        trialDailyReset = LocalDate.now();
        liteOddsReset = LocalDate.now();
    }

    public boolean isPremium() {
        return "premium".equals(tier);
    }

    public boolean isLite() {
        return "lite".equals(tier);
    }

    public boolean isTrial() {
        return "trial".equals(tier);
    }

    public boolean isFree() {
        return "free".equals(tier) || tier == null;
    }
}