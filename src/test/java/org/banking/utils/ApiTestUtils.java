/*
 * Author: Shady Ahmed
 * Date: 2025-09-27
 * Project: Mobile Banking API Testing using RestAssured (E2E)
 * My Linked-in: https://www.linkedin.com/in/shady-ahmed97/.
 */
// ApiTestUtils.java
package org.banking.utils;

import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.util.Map;

public class ApiTestUtils {

    private static final Logger logger = LogManager.getLogger(ApiTestUtils.class);

    // Response validation utilities
    public static void validateStatusCode(Response response, int expectedStatusCode) {
        int actualStatusCode = response.getStatusCode();
        logger.info("Expected status code: {}, Actual status code: {}", expectedStatusCode, actualStatusCode);
        Assert.assertEquals(actualStatusCode, expectedStatusCode,
                "Status code mismatch. Response: " + response.getBody().asString());
    }

    public static void validateStatusCodeRange(Response response, int minStatusCode, int maxStatusCode) {
        int actualStatusCode = response.getStatusCode();
        logger.info("Expected status code range: {}-{}, Actual status code: {}",
                minStatusCode, maxStatusCode, actualStatusCode);
        Assert.assertTrue(actualStatusCode >= minStatusCode && actualStatusCode <= maxStatusCode,
                String.format("Status code %d is not within expected range %d-%d",
                        actualStatusCode, minStatusCode, maxStatusCode));
    }

    public static void validateResponseTime(Response response, long maxResponseTimeMs) {
        long actualResponseTime = response.getTime();
        logger.info("Expected max response time: {}ms, Actual response time: {}ms",
                maxResponseTimeMs, actualResponseTime);
        Assert.assertTrue(actualResponseTime <= maxResponseTimeMs,
                String.format("Response time %dms exceeds maximum allowed time %dms",
                        actualResponseTime, maxResponseTimeMs));
    }

    public static void validateResponseNotNull(Response response) {
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertNotNull(response.getBody(), "Response body should not be null");
    }

    public static void validateJsonResponse(Response response) {
        validateResponseNotNull(response);
        String contentType = response.getHeader("Content-Type");
        Assert.assertTrue(contentType != null && contentType.contains("application/json"),
                "Response should be JSON format");
    }

    public static void validateResponseContainsField(Response response, String fieldPath) {
        Object fieldValue = response.jsonPath().get(fieldPath);
        Assert.assertNotNull(fieldValue, "Response should contain field: " + fieldPath);
    }

    public static void validateResponseFieldValue(Response response, String fieldPath, Object expectedValue) {
        Object actualValue = response.jsonPath().get(fieldPath);
        Assert.assertEquals(actualValue, expectedValue,
                "Field value mismatch for path: " + fieldPath);
    }

    public static void validateResponseHeaders(Response response, Map<String, String> expectedHeaders) {
        for (Map.Entry<String, String> header : expectedHeaders.entrySet()) {
            String actualHeaderValue = response.getHeader(header.getKey());
            Assert.assertEquals(actualHeaderValue, header.getValue(),
                    "Header value mismatch for: " + header.getKey());
        }
    }

    // Error response validation
    public static void validateErrorResponse(Response response, int expectedStatusCode) {
        validateStatusCode(response, expectedStatusCode);
        validateJsonResponse(response);

        // Validate common error response structure
        Assert.assertTrue(response.jsonPath().get("error") != null ||
                        response.jsonPath().get("message") != null ||
                        response.jsonPath().get("errors") != null,
                "Error response should contain error information");
    }

    // Success response validation
    public static void validateSuccessResponse(Response response) {
        validateStatusCodeRange(response, 200, 299);
        validateJsonResponse(response);
        validateResponseTime(response, 5000); // 5 seconds max
    }

    // Pagination validation
    public static void validatePaginatedResponse(Response response) {
        validateSuccessResponse(response);

        // Check for common pagination fields
        if (response.jsonPath().get("page") != null) {
            Assert.assertTrue(response.jsonPath().getInt("page") >= 0, "Page should be >= 0");
        }

        if (response.jsonPath().get("size") != null) {
            Assert.assertTrue(response.jsonPath().getInt("size") > 0, "Size should be > 0");
        }

        if (response.jsonPath().get("totalElements") != null) {
            Assert.assertTrue(response.jsonPath().getLong("totalElements") >= 0,
                    "Total elements should be >= 0");
        }
    }

    // ID validation
    public static Long extractIdFromResponse(Response response) {
        validateSuccessResponse(response);
        Long id = response.jsonPath().getLong("id");
        Assert.assertNotNull(id, "Response should contain an ID");
        Assert.assertTrue(id > 0, "ID should be positive");
        return id;
    }

    // Logging utilities
    public static void logRequest(String method, String endpoint, Object requestBody) {
        logger.info("=== API REQUEST ===");
        logger.info("Method: {}", method);
        logger.info("Endpoint: {}", endpoint);
        if (requestBody != null) {
            logger.info("Request Body: {}", requestBody);
        }
    }

    public static void logResponse(Response response) {
        logger.info("=== API RESPONSE ===");
        logger.info("Status Code: {}", response.getStatusCode());
        logger.info("Response Time: {}ms", response.getTime());
        logger.info("Response Body: {}", response.getBody().asString());
        logger.info("==================");
    }

    // Wait utilities
    public static void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
            logger.info("Waited for {} seconds", seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Wait interrupted: {}", e.getMessage());
        }
    }
}