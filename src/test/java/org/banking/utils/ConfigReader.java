package org.banking.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Logger logger = LogManager.getLogger(ConfigReader.class);
    private static Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try {
            // Load from application.properties
            InputStream inputStream = ConfigReader.class.getClassLoader()
                    .getResourceAsStream("application.properties");
            if (inputStream != null) {
                properties.load(inputStream);
                logger.info("Configuration properties loaded successfully");
            } else {
                logger.error("application.properties file not found");
            }

            // Override with environment-specific properties if exists
            String env = System.getProperty("env", "test");
            InputStream envStream = ConfigReader.class.getClassLoader()
                    .getResourceAsStream("application-" + env + ".properties");
            if (envStream != null) {
                Properties envProps = new Properties();
                envProps.load(envStream);
                properties.putAll(envProps);
                logger.info("Environment-specific properties loaded for: " + env);
            }

        } catch (IOException e) {
            logger.error("Failed to load properties: " + e.getMessage());
            throw new RuntimeException("Failed to load configuration properties", e);
        }
    }

    public static String getProperty(String key) {
        // First check system properties
        String value = System.getProperty(key);
        if (value != null) {
            return value;
        }

        // Then check loaded properties
        value = properties.getProperty(key);
        if (value == null) {
            logger.warn("Property not found: " + key);
        }

        return value;
    }

    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    public static int getIntProperty(String key) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.error("Invalid integer property value for key: " + key + ", value: " + value);
                throw new RuntimeException("Invalid integer property: " + key, e);
            }
        }
        throw new RuntimeException("Property not found: " + key);
    }

    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer property value for key: " + key + ", using default: " + defaultValue);
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static boolean getBooleanProperty(String key) {
        String value = getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        throw new RuntimeException("Property not found: " + key);
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    // Specific configuration getters
    public static String getBaseUrl() {
        return getProperty("base.url");
    }

    public static int getBasePort() {
        return getIntProperty("base.port");
    }

    public static String getBasePath() {
        return getProperty("base.path");
    }

    public static String getAuthToken() {
        return getProperty("auth.token", "");
    }

    public static boolean isLoggingEnabled() {
        return getBooleanProperty("logging.enabled", true);
    }

    public static int getRetryCount() {
        return getIntProperty("retry.count", 2);
    }

    public static int getTestTimeout() {
        return getIntProperty("test.timeout", 30000);
    }

    public static int getParallelThreads() {
        return getIntProperty("test.parallel.threads", 3);
    }

    public static boolean isSchemaValidationEnabled() {
        return getBooleanProperty("schema.validation.enabled", true);
    }

    public static boolean isDatabaseCleanupEnabled() {
        return getBooleanProperty("db.cleanup.enabled", false);
    }

    public static String getTestDataExcelPath() {
        return getProperty("testdata.excel.path", "src/test/resources/testdata/");
    }

    public static String getTestDataJsonPath() {
        return getProperty("testdata.json.path", "src/test/resources/testdata/");
    }

    public static void reloadProperties() {
        properties.clear();
        loadProperties();
        logger.info("Configuration properties reloaded");
    }

    public static void printAllProperties() {
        logger.info("=== Current Configuration Properties ===");
        properties.forEach((key, value) -> {
            // Mask sensitive properties
            if (key.toString().toLowerCase().contains("password") ||
                    key.toString().toLowerCase().contains("token")) {
                logger.info(key + " = " + "*****");
            } else {
                logger.info(key + " = " + value);
            }
        });
        logger.info("=== End Configuration Properties ===");
    }
}