package com.example.SireAI.Repositories;

import com.example.SireAI.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic CRUD operations are inherited from JpaRepository
    // save(), findById(), findAll(), deleteById(), etc.

    // Custom finder methods
    Optional<User> findByUsername(String username);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);

    Optional<User> findByTelegramUserId(String telegramUserId);
    List<User> findByTier(String tier);

    List<User> findByIsBanned(Boolean isBanned);

    List<User> findByExpiryDateBefore(LocalDate date);

    List<User> findByJoinedAtAfter(LocalDateTime date);

    List<User> findByLastMessageDate(LocalDate date);

    // Custom queries with @Query annotation
    @Query("SELECT u FROM User u WHERE u.tier = :tier AND u.isBanned = false")
    List<User> findActiveUsersByTier(@Param("tier") String tier);

    @Query("SELECT u FROM User u WHERE u.lastMessageDate = :date")
    List<User> findUsersActiveToday(@Param("date") LocalDate date);

    @Query("SELECT u FROM User u WHERE u.trialStartDate >= :startDate AND u.trialStartDate <= :endDate")
    List<User> findUsersInTrialPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT u FROM User u WHERE u.termsAccepted = false")
    List<User> findUsersWhoHaventAcceptedTerms();

    @Query("SELECT u FROM User u WHERE u.isBanned = true")
    List<User> findBannedUsers();

    @Query("SELECT u FROM User u WHERE u.expiryDate IS NOT NULL AND u.expiryDate < :currentDate")
    List<User> findExpiredUsers(@Param("currentDate") LocalDate currentDate);

    // Count queries
    long countByTier(String tier);

    long countByIsBanned(Boolean isBanned);

    @Query("SELECT COUNT(u) FROM User u WHERE u.tier = :tier AND u.isBanned = false")
    long countActiveUsersByTier(@Param("tier") String tier);

    // Complex queries for analytics
    @Query("SELECT u FROM User u WHERE u.dailyMessageCount > 0 AND u.lastMessageDate = :date")
    List<User> findActiveUsersToday(@Param("date") LocalDate date);

    @Query("SELECT u FROM User u WHERE u.messageCount > :minMessages")
    List<User> findUsersWithMinimumMessages(@Param("minMessages") int minMessages);

    @Query("SELECT u FROM User u WHERE u.tokens > 0")
    List<User> findUsersWithTokens();

    @Query("SELECT u FROM User u WHERE u.tier = 'trial' AND u.trialDailyPaidCount > 0")
    List<User> findTrialUsersWithActivity();

    @Query("SELECT u FROM User u WHERE u.tier = 'lite' AND u.liteDailyCount > 0")
    List<User> findLiteUsersWithActivity();

    // Search functionality
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.firstName LIKE %:keyword%")
    List<User> searchUsers(@Param("keyword") String keyword);

    // Premium user queries
    @Query("SELECT u FROM User u WHERE u.tier = 'premium' AND u.expiryDate IS NOT NULL")
    List<User> findPremiumUsersWithExpiry();

    // Bot preference queries
    @Query("SELECT u FROM User u WHERE u.botRole IS NOT NULL")
    List<User> findUsersWithBotRole();

    @Query("SELECT u FROM User u WHERE u.botPreference IS NOT NULL")
    List<User> findUsersWithBotPreference();

    // Booking data queries
    @Query("SELECT u FROM User u WHERE u.activeBookingData IS NOT NULL")
    List<User> findUsersWithActiveBooking();

    @Query("SELECT u FROM User u WHERE u.secondBooking IS NOT NULL")
    List<User> findUsersWithSecondBooking();

    // Date-based analytics
    @Query("SELECT u FROM User u WHERE u.joinedAt >= :startDate AND u.joinedAt <= :endDate")
    List<User> findUsersJoinedInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Tier upgrade/downgrade tracking
    @Query("SELECT u FROM User u WHERE u.currentPlan IS NOT NULL AND u.currentPlan != u.tier")
    List<User> findUsersWithPlanMismatch();

    // Daily reset queries
    @Query("SELECT u FROM User u WHERE u.liteDailyReset < :currentDate OR u.trialDailyReset < :currentDate")
    List<User> findUsersNeedingDailyReset(@Param("currentDate") LocalDate currentDate);
}