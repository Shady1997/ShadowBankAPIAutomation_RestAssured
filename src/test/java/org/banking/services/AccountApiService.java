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
import org.banking.dto.AccountDto;

public class AccountApiService {

    private static final Logger logger = LogManager.getLogger(AccountApiService.class);
    private static final String ACCOUNTS_ENDPOINT = "/accounts";

    @Step("Create new account")
    public static Response createAccount(RequestSpecification requestSpec, AccountDto accountDto) {
        logger.info("Creating account for user ID: " + accountDto.getUserId());

        Response response = requestSpec
                .body(accountDto)
                .when()
                .post(ACCOUNTS_ENDPOINT)
                .then()
                .extract()
                .response();

        logger.info("Account creation response status: " + response.getStatusCode());
        return response;
    }

    @Step("Get account by ID: {accountId}")
    public static Response getAccountById(RequestSpecification requestSpec, Long accountId) {
        logger.info("Getting account by ID: " + accountId);

        Response response = requestSpec
                .when()
                .get(ACCOUNTS_ENDPOINT + "/" + accountId)
                .then()
                .extract()
                .response();

        logger.info("Get account by ID response status: " + response.getStatusCode());
        return response;
    }

    @Step("Get all accounts")
    public static Response getAllAccounts(RequestSpecification requestSpec) {
        logger.info("Getting all accounts");

        Response response = requestSpec
                .when()
                .get(ACCOUNTS_ENDPOINT)
                .then()
                .extract()
                .response();

        logger.info("Get all accounts response status: " + response.getStatusCode());
        return response;
    }

    @Step("Get accounts by user ID: {userId}")
    public static Response getAccountsByUserId(RequestSpecification requestSpec, Long userId) {
        logger.info("Getting accounts for user ID: " + userId);

        Response response = requestSpec
                .when()
                .get(ACCOUNTS_ENDPOINT + "/user/" + userId)
                .then()
                .extract()
                .response();

        logger.info("Get accounts by user ID response status: " + response.getStatusCode());
        return response;
    }

    @Step("Update account with ID: {accountId}")
    public static Response updateAccount(RequestSpecification requestSpec, Long accountId, AccountDto accountDto) {
        logger.info("Updating account with ID: " + accountId);

        Response response = requestSpec
                .body(accountDto)
                .when()
                .put(ACCOUNTS_ENDPOINT + "/" + accountId)
                .then()
                .extract()
                .response();

        logger.info("Update account response status: " + response.getStatusCode());
        return response;
    }

    @Step("Delete account with ID: {accountId}")
    public static Response deleteAccount(RequestSpecification requestSpec, Long accountId) {
        logger.info("Deleting account with ID: " + accountId);

        Response response = requestSpec
                .when()
                .delete(ACCOUNTS_ENDPOINT + "/" + accountId)
                .then()
                .extract()
                .response();

        logger.info("Delete account response status: " + response.getStatusCode());
        return response;
    }

    @Step("Get account by account number: {accountNumber}")
    public static Response getAccountByNumber(RequestSpecification requestSpec, String accountNumber) {
        logger.info("Getting account by account number: " + accountNumber);

        Response response = requestSpec
                .when()
                .get(ACCOUNTS_ENDPOINT + "/number/" + accountNumber)
                .then()
                .extract()
                .response();

        logger.info("Get account by number response status: " + response.getStatusCode());
        return response;
    }
}