/*
 * Author: Shady Ahmed
 * Date: 2025-09-27
 * Project: Mobile Banking API Testing using RestAssured (E2E)
 * My Linked-in: https://www.linkedin.com/in/shady-ahmed97/.
 */
package org.banking.tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.banking.base.BaseTest;
import org.banking.dataproviders.DataProviders;
import org.banking.dto.AccountDto;
import org.banking.dto.TransactionDto;
import org.banking.dto.UserDto;
import org.banking.pojo.Account;
import org.banking.pojo.Transaction;
import org.banking.pojo.User;
import org.banking.services.AccountApiService;
import org.banking.services.TransactionApiService;
import org.banking.services.UserApiService;
import org.banking.utils.RetryAnalyzer;
import org.banking.utils.SchemaValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Epic("Banking API")
@Feature("Transaction Management")
public class TransactionApiTests extends BaseTest {

    private static final Logger logger = LogManager.getLogger(TransactionApiTests.class);

    @Test(dataProvider = "validTransactionData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Create Transaction")
    @Description("Test creating a new transaction with valid data")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreateTransactionWithValidData(TransactionDto transactionDto) {
        logger.info("Testing transaction creation with valid data");

        // Setup - create user and account
        Account fromAccount = createTestAccount();
        Account toAccount = createTestAccount();

        // Set account IDs in transaction DTO
        transactionDto.setFromAccountId(fromAccount.getId());
        transactionDto.setToAccountId(toAccount.getId());

        Response response = TransactionApiService.createTransaction(requestSpec, transactionDto);

        // Assert status code
        Assert.assertEquals(response.getStatusCode(), 201, "Transaction creation should return 201");

        // Schema validation
        SchemaValidator.validateTransactionSchema(response);

        // Response validation
        Transaction createdTransaction = response.as(Transaction.class);
        Assert.assertNotNull(createdTransaction.getId(), "Transaction ID should not be null");
        Assert.assertNotNull(createdTransaction.getTransactionReference(),
                "Transaction reference should not be null");
        Assert.assertEquals(createdTransaction.getTransactionType(), transactionDto.getTransactionType(),
                "Transaction type should match");
        Assert.assertEquals(createdTransaction.getAmount(), transactionDto.getAmount(),
                "Amount should match");
        Assert.assertEquals(createdTransaction.getFromAccountId(), fromAccount.getId(),
                "From account ID should match");
        Assert.assertEquals(createdTransaction.getToAccountId(), toAccount.getId(),
                "To account ID should match");

        logger.info("Transaction created successfully with ID: " + createdTransaction.getId());
    }

    @Test(dataProvider = "invalidTransactionData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Create Transaction")
    @Description("Test creating a transaction with invalid data")
    @Severity(SeverityLevel.NORMAL)
    public void testCreateTransactionWithInvalidData(TransactionDto transactionDto) {
        logger.info("Testing transaction creation with invalid data");

        Response response = TransactionApiService.createTransaction(requestSpec, transactionDto);

        // Assert error status code
        Assert.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500,
                "Invalid data should return 4xx status code");

        logger.info("Transaction creation with invalid data returned expected error: " + response.getStatusCode());
    }

    @Test(dataProvider = "validTransactionData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Get Transaction")
    @Description("Test retrieving transaction by ID")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetTransactionById(TransactionDto transactionDto) {
        logger.info("Testing get transaction by ID");

        // Setup - create accounts and transaction
        Account fromAccount = createTestAccount();
        Account toAccount = createTestAccount();

        transactionDto.setFromAccountId(fromAccount.getId());
        transactionDto.setToAccountId(toAccount.getId());

        Response createResponse = TransactionApiService.createTransaction(requestSpec, transactionDto);
        Transaction createdTransaction = createResponse.as(Transaction.class);

        // Test - retrieve by id
        Response getResponse = TransactionApiService.getTransactionById(
                requestSpec, createdTransaction.getId());

        // Assert status code
        Assert.assertEquals(getResponse.getStatusCode(), 200, "Get transaction by reference should return 200");

        // Schema validation
        SchemaValidator.validateTransactionSchema(getResponse);

        // Response validation
        Transaction retrievedTransaction = getResponse.as(Transaction.class);
        Assert.assertEquals(retrievedTransaction.getId(),
                createdTransaction.getId(), "Transaction id should match");
        Assert.assertEquals(retrievedTransaction.getId(), createdTransaction.getId(),
                "Transaction ID should match");

        logger.info("Transaction retrieved by id successfully: " +
                retrievedTransaction.getId());
    }

    @Test(dataProvider = "transactionDataFromExcel", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Create Transaction from Excel")
    @Description("Test creating transactions with data from Excel")
    @Severity(SeverityLevel.NORMAL)
    public void testCreateTransactionFromExcel(Map<String, String> transactionData) {
        logger.info("Testing transaction creation from Excel data");

        // Setup - create accounts
        Account fromAccount = createTestAccount();
        Account toAccount = createTestAccount();

        TransactionDto transactionDto = TransactionDto.builder()
                .transactionType(transactionData.get("transactionType"))
                .amount(new BigDecimal(transactionData.get("amount")))
                .currency(transactionData.get("currency"))
                .description(transactionData.get("description"))
                .fromAccountId(fromAccount.getId())
                .toAccountId(toAccount.getId())
                .build();

        Response response = TransactionApiService.createTransaction(requestSpec, transactionDto);

        // Assert status code
        Assert.assertEquals(response.getStatusCode(), 201, "Transaction creation should return 201");

        // Schema validation
        SchemaValidator.validateTransactionSchema(response);

        Transaction createdTransaction = response.as(Transaction.class);
        Assert.assertEquals(createdTransaction.getTransactionType(), transactionDto.getTransactionType(),
                "Transaction type should match Excel data");

        logger.info("Transaction created from Excel data successfully: " + createdTransaction.getId());
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Story("Transaction Validation")
    @Description("Test transaction with insufficient funds")
    @Severity(SeverityLevel.CRITICAL)
    public void testTransactionWithInsufficientFunds() {
        logger.info("Testing transaction with insufficient funds");

        // Create account with minimal balance
        Account fromAccount = createTestAccountWithBalance(new BigDecimal("50.00"));
        Account toAccount = createTestAccount();

        // Try to transfer more than available balance
        TransactionDto transactionDto = TransactionDto.builder()
                .transactionType("TRANSFER")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .description("Insufficient funds test")
                .fromAccountId(fromAccount.getId())
                .toAccountId(toAccount.getId())
                .build();

        Response response = TransactionApiService.createTransaction(requestSpec, transactionDto);

        // Assert error status code for insufficient funds
        Assert.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500,
                "Insufficient funds should return 4xx status code");

        logger.info("Transaction with insufficient funds returned expected error: " + response.getStatusCode());
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Story("Transaction Validation")
    @Description("Test retrieving all Transactions")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetAllTransactions() {
        logger.info("Testing get all transactions");

        Response response = TransactionApiService.getAllTransactions(requestSpec);

        // Assert status code
        Assert.assertEquals(response.getStatusCode(), 200, "Get all transactions should return 200");

        // Schema validation
        SchemaValidator.validateTransactionListSchema(response);

        // Response validation
        List<Transaction> transactions = response.jsonPath().getList("$", Transaction.class);
        Assert.assertNotNull(transactions, "Transactions list should not be null");

        logger.info("Retrieved " + transactions.size() + " transactions");

    }

    @Test(dataProvider = "validTransactionData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class, groups = {"smoke"})
    @Story("Get Transaction")
    @Description("Test retrieving transaction by Account Id")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetTransactionsByAccountId(TransactionDto transactionDto) {
        logger.info("Testing get transaction by Account Id");

        // Setup - create accounts and transaction
        Account fromAccount = createTestAccount();
        Account toAccount = createTestAccount();

        transactionDto.setFromAccountId(fromAccount.getId());
        transactionDto.setToAccountId(toAccount.getId());

        Response createResponse = TransactionApiService.createTransaction(requestSpec, transactionDto);
        Transaction createdTransaction = createResponse.as(Transaction.class);

        // Test - retrieve by reference
        Response getResponse = TransactionApiService.getTransactionsByAccountId(
                requestSpec, fromAccount.getId());

        // Assert status code
        Assert.assertEquals(getResponse.getStatusCode(), 200, "Get transaction by reference should return 200");

        // Schema validation
        SchemaValidator.validateTransactionListSchema(getResponse);

        // Response validation
        List<Transaction> retrievedTransactions = getResponse.jsonPath().getList("", Transaction.class);

        Transaction retrievedTransaction = retrievedTransactions.get(0);

        Assert.assertEquals(retrievedTransaction.getTransactionReference(),
                createdTransaction.getTransactionReference(), "Transaction reference should match");

        Assert.assertEquals(retrievedTransaction.getId(), createdTransaction.getId(),
                "Transaction ID should match");


        logger.info("Transaction retrieved by reference successfully: " +
                retrievedTransaction.getTransactionReference());
    }

    @Test(dataProvider = "validTransactionData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Get Transaction")
    @Description("Test retrieving transaction by Reference")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetTransactionByReference(TransactionDto transactionDto) {
        logger.info("Testing get transaction by Reference");

        // Setup - create accounts and transaction
        Account fromAccount = createTestAccount();
        Account toAccount = createTestAccount();

        transactionDto.setFromAccountId(fromAccount.getId());
        transactionDto.setToAccountId(toAccount.getId());

        Response createResponse = TransactionApiService.createTransaction(requestSpec, transactionDto);
        Transaction createdTransaction = createResponse.as(Transaction.class);

        // Test - retrieve by Reference
        Response getResponse = TransactionApiService.getTransactionByReference(
                requestSpec, createdTransaction.getTransactionReference());

        // Assert status code
        Assert.assertEquals(getResponse.getStatusCode(), 200, "Get transaction by reference should return 200");

        // Schema validation
        SchemaValidator.validateTransactionSchema(getResponse);

        // Response validation
        Transaction retrievedTransaction = getResponse.as(Transaction.class);
        Assert.assertEquals(retrievedTransaction.getTransactionReference(),
                createdTransaction.getTransactionReference(), "Transaction id should match");
        Assert.assertEquals(retrievedTransaction.getTransactionReference(), createdTransaction.getTransactionReference(),
                "Transaction ID should match");

        logger.info("Transaction retrieved by reference successfully: " +
                retrievedTransaction.getTransactionReference());
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Story("Get Transaction")
    @Description("Test retrieving non-existent transaction")
    @Severity(SeverityLevel.NORMAL)
    public void testGetNonExistentTransaction() {
        logger.info("Testing get non-existent transaction");

        Response response = TransactionApiService.getTransactionById(requestSpec, 99999L);

        // Assert error status code
        Assert.assertEquals(response.getStatusCode(), 404, "Non-existent transaction should return 404");

        logger.info("Non-existent transaction request returned expected 404");
    }

    // Helper methods
    private Account createTestAccount() {
        return createTestAccountWithBalance(new BigDecimal("1000.00"));
    }

    private Account createTestAccountWithBalance(BigDecimal balance) {
        // Create user first
        UserDto userDto = createValidUserDtoForHelper();
        Response userResponse = UserApiService.createUser(requestSpec, userDto);
        User createdUser = userResponse.as(User.class);

        // Create account
        AccountDto accountDto = AccountDto.builder()
                .accountType("SAVINGS")
                .userId(createdUser.getId())
                .balance(balance)
                .creditLimit(new BigDecimal("0.00"))
                .build();

        Response accountResponse = AccountApiService.createAccount(requestSpec, accountDto);
        return accountResponse.as(Account.class);
    }

    private UserDto createValidUserDtoForHelper() {
        com.github.javafaker.Faker faker = new com.github.javafaker.Faker();
        return UserDto.builder()
                .username(faker.name().username())
                .email(faker.internet().emailAddress())
                .password(faker.internet().password(8, 20))
                .fullName(faker.name().fullName())
                .phoneNumber("+201203199419")
                .build();
    }
}