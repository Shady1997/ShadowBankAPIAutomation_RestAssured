/*
 * Author: Shady Ahmed
 * Date: 2025-09-27
 * Project: Mobile Banking API Testing using RestAssured (E2E)
 * My Linked-in: https://www.linkedin.com/in/shady-ahmed97/.
 */
package org.banking.services;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.banking.dto.TransactionDto;

public class TransactionApiService {

    private static final Logger logger = LogManager.getLogger(TransactionApiService.class);
    private static final String TRANSACTIONS_ENDPOINT = "/transactions";

    @Step("Create new transaction")
    public static Response createTransaction(RequestSpecification requestSpec, TransactionDto transactionDto) {
        logger.info("Creating transaction of type: " + transactionDto.getTransactionType());

        Response response = requestSpec
                .body(transactionDto)
                .when()
                .post(TRANSACTIONS_ENDPOINT)
                .then()
                .extract()
                .response();

        logger.info("Transaction creation response status: " + response.getStatusCode());
        return response;
    }

    @Step("Get transaction by ID: {transactionId}")
    public static Response getTransactionById(RequestSpecification requestSpec, Long transactionId) {
        logger.info("Getting transaction by ID: " + transactionId);

        Response response = requestSpec
                .when()
                .get(TRANSACTIONS_ENDPOINT + "/" + transactionId)
                .then()
                .extract()
                .response();

        logger.info("Get transaction by ID response status: " + response.getStatusCode());
        return response;
    }

    @Step("Get all transactions")
    public static Response getAllTransactions(RequestSpecification requestSpec) {
        logger.info("Getting all transactions");

        Response response = requestSpec
                .when()
                .get(TRANSACTIONS_ENDPOINT)
                .then()
                .extract()
                .response();

        logger.info("Get all transactions response status: " + response.getStatusCode());
        return response;
    }

    @Step("Get transactions by account ID: {accountId}")
    public static Response getTransactionsByAccountId(RequestSpecification requestSpec, Long accountId) {
        logger.info("Getting transactions for account ID: " + accountId);

        Response response = requestSpec
                .when()
                .get(TRANSACTIONS_ENDPOINT + "/account/" + accountId)
                .then()
                .extract()
                .response();

        logger.info("Get transactions by account ID response status: " + response.getStatusCode());
        return response;
    }

    @Step("Get transaction by reference: {reference}")
    public static Response getTransactionByReference(RequestSpecification requestSpec, String reference) {
        logger.info("Getting transaction by reference: " + reference);

        Response response = requestSpec
                .when()
                .get(TRANSACTIONS_ENDPOINT + "/reference/" + reference)
                .then()
                .extract()
                .response();

        logger.info("Get transaction by reference response status: " + response.getStatusCode());
        return response;
    }
}