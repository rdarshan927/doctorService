package com.example.doctorservice.client;

import com.example.doctorservice.dto.TokenValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for the user-service token-validation endpoint.
 * <p>
 * Other microservices (appointment-service, payment-service) follow the same pattern.
 * <p>
 * Integration flow:
 * <pre>
 *   Client → doctor-service (JWT in Authorization header)
 *           → UserServiceClient.validateToken(token)
 *           → user-service GET /api/auth/validate
 *           ← { valid, userId, email, role }
 * </pre>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${user-service.url}")
    private String userServiceUrl;

    /**
     * Calls user-service to validate a raw JWT (without "Bearer " prefix).
     * Returns an invalid response instead of throwing on network errors so
     * callers always get a deterministic result.
     *
     * @param bearerToken raw JWT — "Bearer " prefix must be stripped by the controller.
     */
    public TokenValidationResponse validateToken(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            return invalid("No token provided");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<TokenValidationResponse> response = restTemplate.exchange(
                    userServiceUrl + "/api/auth/validate",
                    HttpMethod.GET,
                    entity,
                    TokenValidationResponse.class);

            TokenValidationResponse body = response.getBody();
            if (body == null) {
                return invalid("Empty response from user-service");
            }
            return body;

        } catch (Exception e) {
            log.warn("Token validation call to user-service failed: {}", e.getMessage());
            return invalid("Token validation failed: " + e.getMessage());
        }
    }

    private TokenValidationResponse invalid(String message) {
        return TokenValidationResponse.builder()
                .valid(false)
                .message(message)
                .build();
    }
}
