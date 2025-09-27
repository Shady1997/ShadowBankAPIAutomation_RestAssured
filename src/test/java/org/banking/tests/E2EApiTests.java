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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;

@Epic("Banking API")
@Feature("End-to-End Scenarios")
public class E2EApiTests extends BaseTest {

    private static final Logger logger = LogManager.getLogger(E2EApiTests.class);

    @Test(dataProvider = "e2eTestData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class, groups = {"e2e"})
    @Story("Complete Banking Workflow")
    @Description("End-to-end test: Create user, create account, perform transaction")
    @Severity(SeverityLevel.CRITICAL)
    public void testCompleteUserAccountTransactionWorkflow(UserDto userDto, AccountDto accountDto,
                                                           TransactionDto transactionDto, String scenarioName) {
        logger.info("Starting E2E test scenario: " + scenarioName);

        // Step 1: Create User
        Allure.step("Step 1: Create User", () -> {
            Response userResponse = UserApiService.createUser(requestSpec, userDto);
            Assert.assertEquals(userResponse.getStatusCode(), 201, "User creation should succeed");

            User createdUser = userResponse.as(User.class);
            Assert.assertNotNull(createdUser.getId(), "User ID should not be null");
            logger.info("User created with ID: " + createdUser.getId());

            // Store user ID for next steps
            accountDto.setUserId(createdUser.getId());
        });

        // Step 2: Create Account for User
        Account createdAccount = Allure.step("Step 2: Create Account for User", () -> {
            Response accountResponse = AccountApiService.createAccount(requestSpec, accountDto);
            Assert.assertEquals(accountResponse.getStatusCode(), 201, "Account creation should succeed");

            Account account = accountResponse.as(Account.class);
            Assert.assertNotNull(account.getId(), "Account ID should not be null");
            Assert.assertNotNull(account.getAccountNumber(), "Account number should not be null");
            Assert.assertEquals(account.getUserId(), accountDto.getUserId(), "Account should belong to user");
            logger.info("Account created with ID: " + account.getId() +
                    ", Account Number: " + account.getAccountNumber());

            return account;
        });

        // Step 3: Create a second account for transaction testing (if needed)
        Account secondAccount;
        if ("TRANSFER".equals(transactionDto.getTransactionType())) {
            secondAccount = Allure.step("Step 3: Create Second Account for Transfer", () -> {
                // Create another user for second account
                UserDto secondUserDto = DataProviders.createValidUserDto();
                Response secondUserResponse = UserApiService.createUser(requestSpec, secondUserDto);
                User secondUser = secondUserResponse.as(User.class);

                AccountDto secondAccountDto = AccountDto.builder()
                        .accountType("CHECKING")
                        .userId(secondUser.getId())
                        .balance(new BigDecimal("500.00"))
                        .creditLimit(new BigDecimal("100.00"))
                        .build();

                Response secondAccountResponse = AccountApiService.createAccount(requestSpec, secondAccountDto);
                Account account = secondAccountResponse.as(Account.class);
                logger.info("Second account created with ID: " + account.getId());

                return account;
            });
        } else {
            secondAccount = null;
        }

        // Step 4: Perform Transaction
        Transaction createdTransaction = Allure.step("Step 4: Perform Transaction", () -> {
            // Set account IDs based on transaction type
            transactionDto.setFromAccountId(createdAccount.getId());
            if ("TRANSFER".equals(transactionDto.getTransactionType()) && secondAccount != null) {
                transactionDto.setToAccountId(secondAccount.getId());
            } else {
                transactionDto.setToAccountId(createdAccount.getId());
            }

            Response transactionResponse = TransactionApiService.createTransaction(requestSpec, transactionDto);
            Assert.assertEquals(transactionResponse.getStatusCode(), 201, "Transaction creation should succeed");

            Transaction transaction = transactionResponse.as(Transaction.class);
            Assert.assertNotNull(transaction.getId(), "Transaction ID should not be null");
            Assert.assertNotNull(transaction.getTransactionReference(),
                    "Transaction reference should not be null");
            Assert.assertEquals(transaction.getAmount(), transactionDto.getAmount(),
                    "Transaction amount should match");
            logger.info("Transaction created with ID: " + transaction.getId() +
                    ", Reference: " + transaction.getTransactionReference());

            return transaction;
        });

        // Step 5: Verify Transaction in Account History
        Allure.step("Step 5: Verify Transaction in Account History", () -> {
            Response accountTransactionsResponse = TransactionApiService.getTransactionsByAccountId(
                    requestSpec, createdAccount.getId());
            Assert.assertEquals(accountTransactionsResponse.getStatusCode(), 200,
                    "Get account transactions should succeed");

            List<Transaction> accountTransactions = accountTransactionsResponse.jsonPath()
                    .getList("$", Transaction.class);
            Assert.assertTrue(accountTransactions.size() > 0,
                    "Account should have at least one transaction");

            // Verify our transaction is in the list
            boolean transactionFound = accountTransactions.stream()
                    .anyMatch(t -> t.getId().equals(createdTransaction.getId()));
            Assert.assertTrue(transactionFound, "Created transaction should be in account history");

            logger.info("Transaction verified in account history");
        });

        // Step 6: Verify Transaction by Reference
        Allure.step("Step 6: Verify Transaction by Reference", () -> {
            Response transactionByRefResponse = TransactionApiService.getTransactionByReference(
                    requestSpec, createdTransaction.getTransactionReference());
            Assert.assertEquals(transactionByRefResponse.getStatusCode(), 200,
                    "Get transaction by reference should succeed");

            Transaction retrievedTransaction = transactionByRefResponse.as(Transaction.class);
            Assert.assertEquals(retrievedTransaction.getId(), createdTransaction.getId(),
                    "Retrieved transaction should match created transaction");

            logger.info("Transaction verified by reference lookup");
        });

        logger.info("E2E test scenario completed successfully: " + scenarioName);
    }

    @Test(retryAnalyzer = RetryAnalyzer.class, groups = {"e2e"})
    @Story("Multiple Account Management")
    @Description("Test user with multiple accounts and cross-account transactions")
    @Severity(SeverityLevel.CRITICAL)
    public void testUserWithMultipleAccountsAndTransactions() {
        logger.info("Starting multi-account E2E test");

        // Step 1: Create User
        UserDto userDto = DataProviders.createValidUserDto();
        Response userResponse = UserApiService.createUser(requestSpec, userDto);
        User createdUser = userResponse.as(User.class);
        logger.info("User created: " + createdUser.getId());

        // Step 2: Create Multiple Accounts for Same User
        AccountDto savingsAccountDto = AccountDto.builder()
                .accountType("SAVINGS")
                .userId(createdUser.getId())
                .balance(new BigDecimal("2000.00"))
                .creditLimit(new BigDecimal("0.00"))
                .build();

        AccountDto checkingAccountDto = AccountDto.builder()
                .accountType("CHECKING")
                .userId(createdUser.getId())
                .balance(new BigDecimal("1000.00"))
                .creditLimit(new BigDecimal("500.00"))
                .build();

        Response savingsResponse = AccountApiService.createAccount(requestSpec, savingsAccountDto);
        Account savingsAccount = savingsResponse.as(Account.class);

        Response checkingResponse = AccountApiService.createAccount(requestSpec, checkingAccountDto);
        Account checkingAccount = checkingResponse.as(Account.class);

        logger.info("Created savings account: " + savingsAccount.getId() +
                " and checking account: " + checkingAccount.getId());

        // Step 3: Verify User Has Multiple Accounts
        Response userAccountsResponse = AccountApiService.getAccountsByUserId(requestSpec, createdUser.getId());
        List<Account> userAccounts = userAccountsResponse.jsonPath().getList("$", Account.class);
        Assert.assertTrue(userAccounts.size() >= 2, "User should have at least 2 accounts");

        // Step 4: Transfer Between User's Accounts
        TransactionDto transferDto = TransactionDto.builder()
                .transactionType("TRANSFER")
                .amount(new BigDecimal("300.00"))
                .currency("USD")
                .description("Transfer from savings to checking")
                .fromAccountId(savingsAccount.getId())
                .toAccountId(checkingAccount.getId())
                .build();

        Response transferResponse = TransactionApiService.createTransaction(requestSpec, transferDto);
        Transaction transfer = transferResponse.as(Transaction.class);
        Assert.assertEquals(transferResponse.getStatusCode(), 201, "Transfer should succeed");
        logger.info("Transfer completed: " + transfer.getId());

        // Step 5: Verify Both Accounts Show the Transaction
        Response savingsTransactionsResponse = TransactionApiService.getTransactionsByAccountId(
                requestSpec, savingsAccount.getId());
        Response checkingTransactionsResponse = TransactionApiService.getTransactionsByAccountId(
                requestSpec, checkingAccount.getId());

        List<Transaction> savingsTransactions = savingsTransactionsResponse.jsonPath()
                .getList("$", Transaction.class);
        List<Transaction> checkingTransactions = checkingTransactionsResponse.jsonPath()
                .getList("$", Transaction.class);

        Assert.assertTrue(savingsTransactions.size() > 0, "Savings account should have transactions");
        Assert.assertTrue(checkingTransactions.size() > 0, "Checking account should have transactions");

        logger.info("Multi-account E2E test completed successfully");
    }

    @Test(retryAnalyzer = RetryAnalyzer.class, groups = {"e2e"})
    @Story("Account Lifecycle")
    @Description("Test complete account lifecycle: create, use, update, deactivate")
    @Severity(SeverityLevel.CRITICAL)
    public void testCompleteAccountLifecycle() {
        logger.info("Starting account lifecycle E2E test");

        // Step 1: Create User and Account
        UserDto userDto = DataProviders.createValidUserDto();
        Response userResponse = UserApiService.createUser(requestSpec, userDto);
        User user = userResponse.as(User.class);

        AccountDto accountDto = DataProviders.createValidAccountDto();
        accountDto.setUserId(user.getId());
        Response accountResponse = AccountApiService.createAccount(requestSpec, accountDto);
        Account account = accountResponse.as(Account.class);

        // Step 2: Perform Several Transactions
        for (int i = 0; i < 3; i++) {
            TransactionDto transactionDto = TransactionDto.builder()
                    .transactionType("DEPOSIT")
                    .amount(new BigDecimal("100.00"))
                    .currency("USD")
                    .description("Test deposit " + (i + 1))
                    .fromAccountId(account.getId())
                    .toAccountId(account.getId())
                    .build();

            TransactionApiService.createTransaction(requestSpec, transactionDto);
        }

        // Step 3: Verify Transaction History
        Response transactionsResponse = TransactionApiService.getTransactionsByAccountId(
                requestSpec, account.getId());
        List<Transaction> transactions = transactionsResponse.jsonPath()
                .getList("$", Transaction.class);
        Assert.assertTrue(transactions.size() >= 3, "Account should have at least 3 transactions");

        // Step 4: Update Account Information
        AccountDto updateDto = AccountDto.builder()
                .accountType("CHECKING")
                .userId(user.getId())
                .balance(accountDto.getBalance())
                .creditLimit(new BigDecimal("1000.00"))
                .build();

        Response updateResponse = AccountApiService.updateAccount(requestSpec, account.getId(), updateDto);
        Account updatedAccount = updateResponse.as(Account.class);
        Assert.assertEquals(updatedAccount.getAccountType(), "CHECKING",
                "Account type should be updated");

        // Step 5: Verify Account Still Accessible After Update
        Response getAccountResponse = AccountApiService.getAccountById(requestSpec, account.getId());
        Assert.assertEquals(getAccountResponse.getStatusCode(), 200,
                "Account should be accessible after update");

        logger.info("Account lifecycle E2E test completed successfully");
    }
}