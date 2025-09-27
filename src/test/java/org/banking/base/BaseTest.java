/*
 * Author: Shady Ahmed
 * Date: 2025-09-27
 * Project: Mobile Banking API Testing using RestAssured (E2E)
 * My Linked-in: https://www.linkedin.com/in/shady-ahmed97/.
 */
package org.banking.base;

import com.aventstack.chaintest.plugins.ChainTestListener;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.banking.utils.ConfigReader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import io.qameta.allure.restassured.AllureRestAssured;

@Listeners({org.banking.listeners.AllureTestListener.class, org.banking.listeners.ExtentTestListener.class, ChainTestListener.class})
public class BaseTest {

    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected RequestSpecification requestSpec;

    @BeforeClass(alwaysRun = true)
    public void setupClass() {
        logger.info("Setting up base configuration...");

        // Set base URI from config with safe defaults
        RestAssured.baseURI = ConfigReader.getProperty("base.url", "http://localhost");
        RestAssured.port = ConfigReader.getIntProperty("base.port", 8083);
        RestAssured.basePath = ConfigReader.getProperty("base.path", "/api");

        // Enable request/response logging if needed
        if (ConfigReader.getBooleanProperty("logging.enabled", true)) {
            RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        }

        logger.info("Base configuration completed. URL: " + RestAssured.baseURI + ":" + RestAssured.port + RestAssured.basePath);
    }

    @BeforeMethod(alwaysRun = true)
    public void setupMethod() {
        logger.info("Initializing request specification...");

        requestSpec = RestAssured.given()
                .filter(new AllureRestAssured())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        // Add authentication if needed
        String authToken = ConfigReader.getProperty("auth.token", "");
        if (!authToken.isEmpty()) {
            requestSpec.header("Authorization", "Bearer " + authToken);
            logger.info("Authorization header added");
        }

        logger.info("Request specification initialized successfully");
    }
}