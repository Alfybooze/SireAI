package com.example.SireAI.Services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.context.annotation.Lazy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Lazy
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {

    private final String botUsername = "Bet_Oracle_AI_bot";
    private final String botToken = "8691778800:AAH9cODnSJ6rq3z9YAvlvmVNnwlSSrc53w";

    // Store phone number to Telegram ID mappings
    private final Map<String, String> phoneToTelegramId = new ConcurrentHashMap<>();
    private final Map<String, String> telegramIdToPhone = new ConcurrentHashMap<>();

    @Override
    public String getBotUsername() {
        log.debug("Bot username: {}", botUsername);
        return botUsername;
    }

    @Override
    public String getBotToken() {
        log.debug("Bot token configured: {}", botToken != null ? "YES" : "NO");
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String text = message.getText();
            String chatId = message.getChatId().toString();
            String telegramUserId = message.getFrom().getId().toString();
            String username = message.getFrom().getUserName();

            log.info("Received message from user {}: {}", telegramUserId, text);

            try {
                if (text.startsWith("/start")) {
                    handleStartCommand(chatId, telegramUserId, username);
                } else if (text.startsWith("/link")) {
                    handleLinkCommand(chatId, telegramUserId, text);
                } else if (text.startsWith("/help")) {
                    handleHelpCommand(chatId);
                } else if (text.startsWith("/status")) {
                    handleStatusCommand(chatId, telegramUserId);
                } else {
                    handleUnknownCommand(chatId);
                }
            } catch (Exception e) {
                log.error("Error handling message from user {}", telegramUserId, e);
                sendMessage(chatId, "❌ Sorry, something went wrong. Please try again later.");
            }
        }
    }

    private void handleStartCommand(String chatId, String telegramUserId, String firstName) {
        String welcomeMessage = String.format("""
            👋 Welcome to SireAI, %s!
            
            To complete your registration, please use the /link command with your phone number.
            
            Example: /link +1234567890
            
            Available commands:
            • /link <phone_number> - Link your phone number to this Telegram account
            • /status - Check your account status
            • /help - Show this help message
            
            Need help? Contact our support team.
            """, firstName != null ? firstName : "there");

        sendMessage(chatId, welcomeMessage);
    }

    private void handleLinkCommand(String chatId, String telegramUserId, String text) {
        String[] parts = text.split(" ");
        if (parts.length != 2) {
            sendMessage(chatId, """
                ❌ Invalid format. Please use: /link <phone_number>
                
                Example: /link +1234567890
                """);
            return;
        }

        String phoneNumber = parts[1].trim();
        
        // Validate phone number format
        if (!isValidPhoneNumber(phoneNumber)) {
            sendMessage(chatId, """
                ❌ Invalid phone number format.
                
                Please use international format: +1234567890
                Example: /link +1234567890
                """);
            return;
        }

        // Check if this Telegram ID is already linked to another phone
        String existingPhone = telegramIdToPhone.get(telegramUserId);
        if (existingPhone != null && !existingPhone.equals(phoneNumber)) {
            sendMessage(chatId, String.format("""
                ❌ This Telegram account is already linked to phone: %s
                
                To change your linked phone number, please contact support.
                """, existingPhone));
            return;
        }

        // Check if this phone number is already linked to another Telegram ID
        String existingTelegramId = phoneToTelegramId.get(phoneNumber);
        if (existingTelegramId != null && !existingTelegramId.equals(telegramUserId)) {
            sendMessage(chatId, """
                ❌ This phone number is already linked to another Telegram account.
                
                If this is your phone number, please contact support.
                """);
            return;
        }

        // Link the phone number to Telegram ID
        phoneToTelegramId.put(phoneNumber, telegramUserId);
        telegramIdToPhone.put(telegramUserId, phoneNumber);

        log.info("Successfully linked phone {} to Telegram ID {}", phoneNumber, telegramUserId);

        sendMessage(chatId, String.format("""
            ✅ Successfully linked your phone number: %s
            
            You can now complete your registration on the website.
            
            Your Telegram ID is: %s
            """, phoneNumber, telegramUserId));
    }

    private void handleHelpCommand(String chatId) {
        String helpMessage = """
            📋 SireAI Bot Commands:
            
            • /link <phone_number> - Link your phone number to this Telegram account
            • /status - Check your account status
            • /help - Show this help message
            
            Example: /link +1234567890
            
            Need more help? Contact our support team.
            """;

        sendMessage(chatId, helpMessage);
    }

    private void handleStatusCommand(String chatId, String telegramUserId) {
        String phoneNumber = telegramIdToPhone.get(telegramUserId);
        
        if (phoneNumber != null) {
            sendMessage(chatId, String.format("""
                📊 Your Account Status:
                
                ✅ Telegram ID: %s
                ✅ Linked Phone: %s
                ✅ Account Status: Ready for registration
                """, telegramUserId, phoneNumber));
        } else {
            sendMessage(chatId, String.format("""
                📊 Your Account Status:
                
                ✅ Telegram ID: %s
                ❌ No phone number linked
                
                Use /link <phone_number> to link your phone number.
                  """, telegramUserId));
        }
    }

    private void handleUnknownCommand(String chatId) {
        sendMessage(chatId, """
            ❓ Unknown command.
            
            Available commands:
            • /link <phone_number> - Link your phone number
            • /status - Check your status
            • /help - Show help
            
            Example: /link +1234567890
            """);
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Basic phone number validation
        return phoneNumber != null && 
               phoneNumber.matches("^\\+?[1-9]\\d{1,14}$") &&
               phoneNumber.length() >= 10 && 
               phoneNumber.length() <= 15;
    }

    /**
     * Send message to Telegram user
     */
    public boolean sendMessage(String chatId, String text) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(text);
            message.setParseMode("Markdown");
            
            execute(message);
            log.info("Message sent to chat {}: {}", chatId, text);
            return true;
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat {}: {}", chatId, e.getMessage());
            return false;
        }
    }

    /**
     * Send OTP message to phone number
     */
    public boolean sendOTPToPhone(String phoneNumber, String otpCode) {
        String telegramUserId = phoneToTelegramId.get(phoneNumber);
        if (telegramUserId == null) {
            log.warn("No Telegram user found for phone: {}", phoneNumber);
            return false;
        }

        String message = String.format("""
            🔐 Your SireAI Verification Code
            
            **%s**
            
            This code will expire in 15 minutes.
            
            If you didn't request this, please ignore this message.
            """, otpCode);

        return sendMessage(telegramUserId, message);
    }

    /**
     * Check if user has messaged our bot
     */
    public boolean hasUserMessagedBot(String telegramUserId, String phoneNumber) {
        if (phoneNumber != null) {
            // Check if phone is linked to this Telegram ID
            String linkedTelegramId = phoneToTelegramId.get(phoneNumber);
            return linkedTelegramId != null && linkedTelegramId.equals(telegramUserId);
        }
        
        // Just check if this Telegram ID has interacted with our bot
        return telegramIdToPhone.containsKey(telegramUserId);
    }

    /**
     * Get Telegram ID by phone number
     */
    public String getTelegramIdByPhoneNumber(String phoneNumber) {
        return phoneToTelegramId.get(phoneNumber);
    }

    /**
     * Get phone number by Telegram ID
     */
    public String getPhoneNumberByTelegramId(String telegramUserId) {
        return telegramIdToPhone.get(telegramUserId);
    }

    /**
     * Remove phone-Telegram mapping
     */
    public void removePhoneMapping(String phoneNumber) {
        String telegramUserId = phoneToTelegramId.remove(phoneNumber);
        if (telegramUserId != null) {
            telegramIdToPhone.remove(telegramUserId);
            log.info("Removed phone mapping for: {}", phoneNumber);
        }
    }
}