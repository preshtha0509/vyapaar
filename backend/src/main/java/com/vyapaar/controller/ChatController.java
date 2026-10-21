package com.vyapaar.controller;

import com.vyapaar.dto.ChatRequest;
import com.vyapaar.dto.ChatResponse;
import com.vyapaar.service.ChatService;
import com.vyapaar.util.ChatRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;

/**
 * The order of checks matters: feature switched on, then rate limit, then access code, then the
 * question itself. Rate limiting comes before the code check on purpose, so that guessing the
 * access code is also rate limited.
 *
 * Error replies use ordinary HTTP codes and never leak internal detail to the browser.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private static final int MAX_QUESTION_LENGTH = 500;

    private final ChatService chatService;
    private final String accessCode;
    private final ChatRateLimiter rateLimiter = new ChatRateLimiter(20, 60_000);

    public ChatController(ChatService chatService, @Value("${chat.access-code:}") String accessCode) {
        this.chatService = chatService;
        this.accessCode = accessCode;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> ask(
            @RequestHeader(value = "X-Chat-Key", required = false) String key,
            @RequestBody ChatRequest request) {

        if (accessCode == null || accessCode.isBlank()) {
            return reply(HttpStatus.SERVICE_UNAVAILABLE, "The assistant is not switched on yet.");
        }
        if (!rateLimiter.tryAcquire()) {
            return reply(HttpStatus.TOO_MANY_REQUESTS, "Too many questions. Please wait a minute.");
        }
        if (!codeMatches(key)) {
            return reply(HttpStatus.UNAUTHORIZED, "Wrong access code.");
        }
        if (!chatService.isConfigured()) {
            log.error("Chat is switched on but CHAT_API_KEY or CHAT_DB_USERNAME is missing");
            return reply(HttpStatus.SERVICE_UNAVAILABLE, "The assistant is not set up correctly.");
        }

        String question = request == null || request.question() == null ? "" : request.question().trim();
        if (question.isEmpty() || question.length() > MAX_QUESTION_LENGTH) {
            return reply(HttpStatus.BAD_REQUEST, "Please ask a question of up to 500 characters.");
        }

        try {
            return ResponseEntity.ok(chatService.ask(question));
        } catch (IllegalArgumentException e) {
            log.warn("Query rejected by guard: {}", e.getMessage());
            return reply(HttpStatus.BAD_REQUEST,
                    "I could not turn that into a safe query. Please rephrase the question.");
        } catch (SQLException e) {
            log.warn("Chat query failed: {}", e.getMessage());
            return reply(HttpStatus.BAD_REQUEST, "That query did not work. Please rephrase the question.");
        } catch (IllegalStateException e) {
            log.error("Chat is not set up correctly: {}", e.getMessage());
            return reply(HttpStatus.SERVICE_UNAVAILABLE, "The assistant is not set up correctly.");
        } catch (RestClientException e) {
            log.error("AI service call failed: {}", e.getMessage());
            return reply(HttpStatus.BAD_GATEWAY, "The AI service is not reachable right now.");
        }
    }

    // Constant-time comparison, so the response time does not leak how much of the code matched.
    private boolean codeMatches(String key) {
        byte[] expected = accessCode.getBytes(StandardCharsets.UTF_8);
        byte[] given = (key == null ? "" : key).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, given);
    }

    private ResponseEntity<ChatResponse> reply(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ChatResponse.message(message));
    }
}
