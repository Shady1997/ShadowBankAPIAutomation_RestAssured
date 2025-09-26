package org.banking.utils;

import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;

public class SchemaValidator {

    private static final Logger logger = LogManager.getLogger(SchemaValidator.class);

    public static void validateSchema(Response response, String schemaFileName) {
        try {
            InputStream schemaStream = SchemaValidator.class.getClassLoader()
                    .getResourceAsStream("schemas/" + schemaFileName);

            if (schemaStream == null) {
                throw new RuntimeException("Schema file not found: " + schemaFileName);
            }

            response.then().body(matchesJsonSchema(schemaStream));
            logger.info("Schema validation passed for: " + schemaFileName);

        } catch (Exception e) {
            logger.error("Schema validation failed for: " + schemaFileName + " - " + e.getMessage());
            throw new AssertionError("Schema validation failed: " + e.getMessage(), e);
        }
    }

    public static void validateUserSchema(Response response) {
        validateSchema(response, "user-schema.json");
    }

    public static void validateAccountSchema(Response response) {
        validateSchema(response, "account-schema.json");
    }

    public static void validateTransactionSchema(Response response) {
        validateSchema(response, "transaction-schema.json");
    }

    public static void validateUserListSchema(Response response) {
        validateSchema(response, "user-list-schema.json");
    }

    public static void validateAccountListSchema(Response response) {
        validateSchema(response, "account-list-schema.json");
    }

    public static void validateTransactionListSchema(Response response) {
        validateSchema(response, "transaction-list-schema.json");
    }
}