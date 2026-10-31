package com.financecoach.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final String apiKey;
    private final String model;
    private final String fallbackModel;
    private final String apiUrl;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiClient(
            @Value("${financecoach.ai.api-key:}") String apiKey,
            @Value("${financecoach.ai.model:gemini-3.5-flash}") String model,
            @Value("${financecoach.ai.fallback-model:gemini-3.5-flash-lite}") String fallbackModel,
            @Value("${financecoach.ai.api-url:https://generativelanguage.googleapis.com/v1beta/models}") String apiUrl,
            @Value("${financecoach.ai.timeout-seconds:15}") int timeoutSeconds,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = model != null ? model.trim() : "gemini-3.5-flash";
        this.fallbackModel = fallbackModel != null ? fallbackModel.trim() : "gemini-3.5-flash-lite";
        this.apiUrl = apiUrl != null ? apiUrl.trim() : "https://generativelanguage.googleapis.com/v1beta/models";
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    public boolean isConfigured() {
        return !apiKey.isEmpty();
    }

    public Optional<String> generateContent(String systemInstruction, String userPrompt) {
        if (!isConfigured()) {
            log.warn("Gemini API key is not configured; AI coach will use deterministic fallback.");
            return Optional.empty();
        }

        // Try primary model
        Optional<String> primaryResult = callModel(this.model, systemInstruction, userPrompt);
        if (primaryResult.isPresent()) {
            return primaryResult;
        }

        // If primary model failed and fallback model is specified, try fallback model
        if (!fallbackModel.isEmpty() && !fallbackModel.equalsIgnoreCase(this.model)) {
            log.info("Attempting AI call with fallback model: {}", fallbackModel);
            return callModel(this.fallbackModel, systemInstruction, userPrompt);
        }

        return Optional.empty();
    }

    private Optional<String> callModel(String modelName, String systemInstruction, String userPrompt) {
        try {
            String url = String.format("%s/%s:generateContent?key=%s", apiUrl, modelName, apiKey);

            Map<String, Object> payload = new HashMap<>();

            if (systemInstruction != null && !systemInstruction.isBlank()) {
                payload.put("system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemInstruction))
                ));
            }

            payload.put("contents", List.of(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", userPrompt))
            )));

            payload.put("generationConfig", Map.of(
                    "temperature", 0.4,
                    "maxOutputTokens", 800
            ));

            String requestBody = objectMapper.writeValueAsString(payload);

            String responseBody = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                log.warn("Gemini returned empty response for model {}", modelName);
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    String text = parts.get(0).path("text").asText("");
                    if (!text.isBlank()) {
                        return Optional.of(text.trim());
                    }
                }
            }

            log.warn("Gemini response missing candidate parts: {}", responseBody);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Gemini API call failed for model {}: {}", modelName, e.getMessage());
            return Optional.empty();
        }
    }
}

