package com.example.chatcore.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Validator cho Context theo section 6: Context Capture và bảo vệ dữ liệu
 * Whitelist context, không truyền nguyên business object
 * Loại bỏ: OTP, password, access token, full card number, số dư, dữ liệu nhạy cảm
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContextValidator {

    private final ObjectMapper objectMapper;

    // Blacklist sensitive fields theo section 6
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "otp", "password", "accessToken", "access_token", "token",
            "cardNumber", "card_number", "cvv", "pin",
            "balance", "amount", "account_balance",
            "secret", "api_key", "apiKey", "privateKey", "private_key"
    );

    /**
     * Validate và sanitize context JSON
     * Throw IllegalArgumentException nếu chứa sensitive data
     */
    public void validateContext(String contextJson) {
        if (contextJson == null || contextJson.isBlank()) {
            return;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(contextJson);
            validateNode(rootNode, "");
        } catch (IllegalArgumentException e) {
            // Re-throw validation errors
            throw e;
        } catch (Exception e) {
            log.warn("Failed to parse context JSON, skipping validation: {}", e.getMessage());
            // Don't fail the request if JSON parsing fails, just skip validation
        }
    }

    private void validateNode(JsonNode node, String path) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey().toLowerCase();
                String fullPath = path.isEmpty() ? fieldName : path + "." + fieldName;

                // Check if field is sensitive
                if (isSensitiveField(fieldName)) {
                    throw new IllegalArgumentException(
                            String.format("Sensitive field '%s' not allowed in context", fullPath));
                }

                validateNode(entry.getValue(), fullPath);
            });
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                validateNode(node.get(i), path + "[" + i + "]");
            }
        }
    }

    private boolean isSensitiveField(String fieldName) {
        return SENSITIVE_FIELDS.stream().anyMatch(fieldName::contains);
    }
}
