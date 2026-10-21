package com.vyapaar.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Calls the Anthropic Messages API with Spring's own RestClient, so the module adds no new
 * dependency to the fixed tech stack. The API key is never logged and never returned to the
 * browser.
 */
@Service
public class ClaudeClient {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION = "2023-06-01";

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public ClaudeClient(@Value("${chat.api-key:}") String apiKey,
                        @Value("${chat.model:claude-haiku-5-5}") String model) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(30));

        this.restClient = RestClient.builder().requestFactory(factory).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    /** True when CHAT_API_KEY is configured, so the controller can answer 503 before spending anything. */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String complete(String system, String userMessage, int maxTokens) {
        if (!isConfigured()) {
            throw new IllegalStateException("CHAT_API_KEY is not set");
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "system", system,
                "messages", List.of(Map.of("role", "user", "content", userMessage)));

        Map<?, ?> response = restClient.post()
                .uri(API_URL)
                .header("x-api-key", apiKey)
                .header("anthropic-version", API_VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        Object content = response == null ? null : response.get("content");
        if (content instanceof List<?> blocks && !blocks.isEmpty() && blocks.get(0) instanceof Map<?, ?> first) {
            return String.valueOf(first.get("text")).trim();
        }
        throw new IllegalStateException("The AI service returned an empty response");
    }
}
