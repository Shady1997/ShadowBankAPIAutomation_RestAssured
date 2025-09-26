package org.banking.tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.banking.base.BaseTest;
import org.banking.dataproviders.DataProviders;
import org.banking.dto.AccountDto;
import org.banking.dto.UserDto;
import org.banking.pojo.Account;
import org.banking.pojo.User;
import org.banking.services.AccountApiService;
import org.banking.services.UserApiService;
import org.banking.utils.RetryAnalyzer;
import org.banking.utils.SchemaValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

@Epic("Banking API")
@Feature("Account Management")
public class AccountApiTests extends BaseTest {

    private static final Logger logger = LogManager.getLogger(AccountApiTests.class);

    @Test(dataProvider = "validAccountData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Create Account")
    @Description("Test creating a new account with valid data")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreateAccountWithValidData(AccountDto accountDto) {
        logger.info("Testing account creation with valid data");

        // First create a user for the account
        UserDto userDto = DataProviders.createValidUserDto();
        Response userResponse = UserApiService.createUser(requestSpec, userDto);
        User createdUser = userResponse.as(User.class);

        // Set the user ID in account DTO
        accountDto.setUserId(createdUser.getId());

        Response response = AccountApiService.createAccount(requestSpec, accountDto);

        // Assert status code
        Assert.assertEquals(response.getStatusCode(), 201, "Account creation should return 201");

        // Schema validation
        SchemaValidator.validateAccountSchema(response);

        // Response validation
        Account createdAccount = response.as(Account.class);
        Assert.assertNotNull(createdAccount.getId(), "Account ID should not be null");
        Assert.assertNotNull(createdAccount.getAccountNumber(), "Account number should not be null");
        Assert.assertEquals(createdAccount.getAccountType(), accountDto.getAccountType(), "Account type should match");
        Assert.assertEquals(createdAccount.getUserId(), createdUser.getId(), "User ID should match");

        logger.info("Account created successfully with ID: " + createdAccount.getId());
    }

    @Test(dataProvider = "invalidAccountData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Create Account")
    @Description("Test creating an account with invalid data")
    @Severity(SeverityLevel.NORMAL)
    public void testCreateAccountWithInvalidData(AccountDto accountDto) {
        logger.info("Testing account creation with invalid data");

        Response response = AccountApiService.createAccount(requestSpec, accountDto);

        // Assert error status code
        Assert.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500,
                "Invalid data should return 4xx status code");

        logger.info("Account creation with invalid data returned expected error: " + response.getStatusCode());
    }

    @Test(dataProvider = "validAccountData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Get Account")
    @Description("Test retrieving account by ID")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetAccountById(AccountDto accountDto) {
        logger.info("Testing get account by ID");

        // Setup - create user and account
        UserDto userDto = DataProviders.createValidUserDto();
        Response userResponse = UserApiService.createUser(requestSpec, userDto);
        User createdUser = userResponse.as(User.class);

        accountDto.setUserId(createdUser.getId());
        Response createResponse = AccountApiService.createAccount(requestSpec, accountDto);
        Account createdAccount = createResponse.as(Account.class);

        // Test - retrieve account
        Response getResponse = AccountApiService.getAccountById(requestSpec, createdAccount.getId());

        // Assert status code
        Assert.assertEquals(getResponse.getStatusCode(), 200, "Get account should return 200");

        // Schema validation
        SchemaValidator.validateAccountSchema(getResponse);

        // Response validation
        Account retrievedAccount = getResponse.as(Account.class);
        Assert.assertEquals(retrievedAccount.getId(), createdAccount.getId(), "Account ID should match");
        Assert.assertEquals(retrievedAccount.getAccountNumber(), createdAccount.getAccountNumber(),
                "Account number should match");

        logger.info("Account retrieved successfully: " + retrievedAccount.getId());
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Story("Get Account")
    @Description("Test retrieving non-existent account")
    @Severity(SeverityLevel.NORMAL)
    public void testGetNonExistentAccount() {
        logger.info("Testing get non-existent account");

        Response response = AccountApiService.getAccountById(requestSpec, 99999L);

        // Assert error status code
        Assert.assertEquals(response.getStatusCode(), 404, "Non-existent account should return 404");

        logger.info("Non-existent account request returned expected 404");
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Story("Get Accounts")
    @Description("Test retrieving all accounts")
    @Severity(SeverityLevel.NORMAL)
    public void testGetAllAccounts() {
        logger.info("Testing get all accounts");

        Response response = AccountApiService.getAllAccounts(requestSpec);

        // Assert status code
        Assert.assertEquals(response.getStatusCode(), 200, "Get all accounts should return 200");

        // Schema validation
        SchemaValidator.validateAccountListSchema(response);

        // Response validation
        List<Account> accounts = response.jsonPath().getList("$", Account.class);
        Assert.assertNotNull(accounts, "Accounts list should not be null");

        logger.info("Retrieved " + accounts.size() + " accounts");
    }

    @Test(dataProvider = "validAccountData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Get Accounts by User")
    @Description("Test retrieving accounts by user ID")
    @Severity(SeverityLevel.NORMAL)
    public void testGetAccountsByUserId(AccountDto accountDto) {
        logger.info("Testing get accounts by user ID");

        // Setup - create user and account
        UserDto userDto = DataProviders.createValidUserDto();
        Response userResponse = UserApiService.createUser(requestSpec, userDto);
        User createdUser = userResponse.as(User.class);

        accountDto.setUserId(createdUser.getId());
        AccountApiService.createAccount(requestSpec, accountDto);

        // Test - get accounts by user ID
        Response response = AccountApiService.getAccountsByUserId(requestSpec, createdUser.getId());

        // Assert status code
        Assert.assertEquals(response.getStatusCode(), 200, "Get accounts by user ID should return 200");

        // Schema validation
        SchemaValidator.validateAccountListSchema(response);

        // Response validation
        List<Account> accounts = response.jsonPath().getList("$", Account.class);
        Assert.assertNotNull(accounts, "Accounts list should not be null");
        Assert.assertTrue(accounts.size() > 0, "User should have at least one account");

        // Verify all accounts belong to the user
        for (Account account : accounts) {
            Assert.assertEquals(account.getUserId(), createdUser.getId(),
                    "All accounts should belong to the specified user");
        }

        logger.info("Retrieved " + accounts.size() + " accounts for user: " + createdUser.getId());
    }

    @Test(dataProvider = "validAccountData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Update Account")
    @Description("Test updating account information")
    @Severity(SeverityLevel.NORMAL)
    public void testUpdateAccount(AccountDto accountDto) {
        logger.info("Testing account update");

        // Setup - create user and account
        UserDto userDto = DataProviders.createValidUserDto();
        Response userResponse = UserApiService.createUser(requestSpec, userDto);
        User createdUser = userResponse.as(User.class);

        accountDto.setUserId(createdUser.getId());
        Response createResponse = AccountApiService.createAccount(requestSpec, accountDto);
        Account createdAccount = createResponse.as(Account.class);

        // Update account data
        AccountDto updateDto = AccountDto.builder()
                .accountType("CHECKING") // Change account type
                .balance(accountDto.getBalance())
                .userId(createdUser.getId())
                .creditLimit(accountDto.getCreditLimit())
                .status(accountDto.getStatus())
                .build();

        // Update the account
        Response updateResponse = AccountApiService.updateAccount(requestSpec, createdAccount.getId(), updateDto);

        // Assert status code
        Assert.assertEquals(updateResponse.getStatusCode(), 200, "Update account should return 200");

        // Schema validation
        SchemaValidator.validateAccountSchema(updateResponse);

        // Response validation
        Account updatedAccount = updateResponse.as(Account.class);
        Assert.assertEquals(updatedAccount.getId(), createdAccount.getId(), "Account ID should remain same");
        Assert.assertEquals(updatedAccount.getAccountType(), updateDto.getAccountType(),
                "Account type should be updated");

        logger.info("Account updated successfully: " + updatedAccount.getId());
    }

    @Test(dataProvider = "validAccountData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Delete Account")
    @Description("Test deleting an account")
    @Severity(SeverityLevel.CRITICAL)
    public void testDeleteAccount(AccountDto accountDto) {
        logger.info("Testing account deletion");

        // Setup - create user and account
        UserDto userDto = DataProviders.createValidUserDto();
        Response userResponse = UserApiService.createUser(requestSpec, userDto);
        User createdUser = userResponse.as(User.class);

        accountDto.setUserId(createdUser.getId());
        Response createResponse = AccountApiService.createAccount(requestSpec, accountDto);
        Account createdAccount = createResponse.as(Account.class);

        // Delete the account
        Response deleteResponse = AccountApiService.deleteAccount(requestSpec, createdAccount.getId());

        // Assert status code
        Assert.assertEquals(deleteResponse.getStatusCode(), 204, "Delete account should return 204");

        // Verify account is deleted by trying to retrieve it
        Response getResponse = AccountApiService.getAccountById(requestSpec, createdAccount.getId());
        Assert.assertEquals(getResponse.getStatusCode(), 404, "Deleted account should return 404");

        logger.info("Account deleted successfully: " + createdAccount.getId());
    }

    @Test(dataProvider = "validAccountData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Get Account by Number")
    @Description("Test retrieving account by account number")
    @Severity(SeverityLevel.NORMAL)
    public void testGetAccountByNumber(AccountDto accountDto) {
        logger.info("Testing get account by account number");

        // Setup - create user and account
        UserDto userDto = DataProviders.createValidUserDto();
        Response userResponse = UserApiService.createUser(requestSpec, userDto);
        User createdUser = userResponse.as(User.class);

        accountDto.setUserId(createdUser.getId());
        Response createResponse = AccountApiService.createAccount(requestSpec, accountDto);
        Account createdAccount = createResponse.as(Account.class);

        // Test - retrieve by account number
        Response getResponse = AccountApiService.getAccountByNumber(requestSpec, createdAccount.getAccountNumber());

        // Assert status code
        Assert.assertEquals(getResponse.getStatusCode(), 200, "Get account by number should return 200");

        // Schema validation
        SchemaValidator.validateAccountSchema(getResponse);

        // Response validation
        Account retrievedAccount = getResponse.as(Account.class);
        Assert.assertEquals(retrievedAccount.getAccountNumber(), createdAccount.getAccountNumber(),
                "Account number should match");
        Assert.assertEquals(retrievedAccount.getId(), createdAccount.getId(), "Account ID should match");

        logger.info("Account retrieved by number successfully: " + retrievedAccount.getAccountNumber());
    }

    @Test(dataProvider = "accountDataFromExcel", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Create Account from Excel")
    @Description("Test creating accounts with data from Excel")
    @Severity(SeverityLevel.NORMAL)
    public void testCreateAccountFromExcel(Map<String, String> accountData) {
        logger.info("Testing account creation from Excel data");

        // First create a user
        UserDto userDto = DataProviders.createValidUserDto();
        Response userResponse = UserApiService.createUser(requestSpec, userDto);
        User createdUser = userResponse.as(User.class);

        AccountDto accountDto = AccountDto.builder()
                .accountType(accountData.get("accountType"))
                .status(accountData.get("status"))
                .userId(createdUser.getId())
                .creditLimit(new java.math.BigDecimal(accountData.get("initialBalance")))
                .balance(new java.math.BigDecimal(accountData.get("overdraftLimit")))
                .build();

        Response response = AccountApiService.createAccount(requestSpec, accountDto);

        // Assert status code
        Assert.assertEquals(response.getStatusCode(), 201, "Account creation should return 201");

        // Schema validation
        SchemaValidator.validateAccountSchema(response);

        Account createdAccount = response.as(Account.class);
        Assert.assertEquals(createdAccount.getAccountType(), accountDto.getAccountType(),
                "Account type should match Excel data");

        logger.info("Account created from Excel data successfully: " + createdAccount.getId());
    }
}