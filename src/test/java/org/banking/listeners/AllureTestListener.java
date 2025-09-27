/*
 * Author: Shady Ahmed
 * Date: 2025-09-27
 * Project: Mobile Banking API Testing using RestAssured (E2E)
 * My Linked-in: https://www.linkedin.com/in/shady-ahmed97/.
 */
package org.banking.listeners;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class AllureTestListener implements ITestListener {

    private static final Logger logger = LogManager.getLogger(AllureTestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("Starting test: " + result.getMethod().getMethodName());
        Allure.step("Test started: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test passed: " + result.getMethod().getMethodName());
        Allure.step("Test completed successfully");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("Test failed: " + result.getMethod().getMethodName() +
                " - Reason: " + result.getThrowable().getMessage());

        // Add failure details to Allure
        Allure.step("Test failed: " + result.getThrowable().getMessage());

        // Attach failure details
        String failureDetails = "Test: " + result.getMethod().getMethodName() + "\n" +
                "Class: " + result.getTestClass().getName() + "\n" +
                "Exception: " + result.getThrowable().toString();

        Allure.addAttachment("Failure Details", "text/plain", failureDetails);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Test skipped: " + result.getMethod().getMethodName());
        Allure.step("Test skipped: " + result.getMethod().getMethodName());
    }
}