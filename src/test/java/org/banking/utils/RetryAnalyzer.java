/*
 * Author: Shady Ahmed
 * Date: 2025-09-27
 * Project: Mobile Banking API Testing using RestAssured (E2E)
 * My Linked-in: https://www.linkedin.com/in/shady-ahmed97/.
 */
package org.banking.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger logger = LogManager.getLogger(RetryAnalyzer.class);
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = Integer.parseInt(
            System.getProperty("retry.count", "1")
    );

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            logger.warn("Retrying test: " + result.getMethod().getMethodName() +
                    " - Attempt: " + retryCount + "/" + MAX_RETRY_COUNT);
            return true;
        }
        return false;
    }

    public static int getMaxRetryCount() {
        return MAX_RETRY_COUNT;
    }
}