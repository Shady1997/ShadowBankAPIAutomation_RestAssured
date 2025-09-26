package org.banking.tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.banking.base.BaseTest;
import org.banking.dataproviders.DataProviders;
import org.banking.dto.UserDto;
import org.banking.pojo.User;
import org.banking.services.UserApiService;
import org.banking.utils.RetryAnalyzer;
import org.banking.utils.SchemaValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

@Epic("Banking API")
@Feature("User Management")
public class UserApiTests extends BaseTest {

    private static final Logger logger = LogManager.getLogger(UserApiTests.class);

    @Test(dataProvider = "validUserData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Create User")
    @Description("Test creating a new user with valid data")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreateUserWithValidData(UserDto userDto) {
        logger.info("Testing user creation with valid data");

        Response response = UserApiService.createUser(requestSpec, userDto);

        // Assert status code
        Assert.assertEquals(response.getStatusCode(), 201, "User creation should return 201");

        // Schema validation
        SchemaValidator.validateUserSchema(response);

        // Response validation
        User createdUser = response.as(User.class);
        Assert.assertNotNull(createdUser.getId(), "User ID should not be null");
        Assert.assertEquals(createdUser.getUsername(), userDto.getUsername(), "Username should match");
        Assert.assertEquals(createdUser.getEmail(), userDto.getEmail(), "Email should match");

        logger.info("User created successfully with ID: " + createdUser.getId());
    }

    @Test(dataProvider = "invalidUserData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Create User")
    @Description("Test creating a user with invalid data")
    @Severity(SeverityLevel.NORMAL)
    public void testCreateUserWithInvalidData(UserDto userDto) {
        logger.info("Testing user creation with invalid data");

        Response response = UserApiService.createUser(requestSpec, userDto);

        // Assert error status code
        Assert.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500,
                "Invalid data should return 4xx status code");

        logger.info("User creation with invalid data returned expected error: " + response.getStatusCode());
    }

    @Test(dependsOnMethods = "testCreateUserWithValidData",
            dataProvider = "validUserData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Get User")
    @Description("Test retrieving user by ID")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetUserById(UserDto userDto) {
        logger.info("Testing get user by ID");

        // First create a user
        Response createResponse = UserApiService.createUser(requestSpec, userDto);
        User createdUser = createResponse.as(User.class);

        // Then retrieve it
        Response getResponse = UserApiService.getUserById(requestSpec, createdUser.getId());

        // Assert status code
        Assert.assertEquals(getResponse.getStatusCode(), 200, "Get user should return 200");

        // Schema validation
        SchemaValidator.validateUserSchema(getResponse);

        // Response validation
        User retrievedUser = getResponse.as(User.class);
        Assert.assertEquals(retrievedUser.getId(), createdUser.getId(), "User ID should match");
        Assert.assertEquals(retrievedUser.getUsername(), createdUser.getUsername(), "Username should match");

        logger.info("User retrieved successfully: " + retrievedUser.getId());
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Story("Get User")
    @Description("Test retrieving non-existent user")
    @Severity(SeverityLevel.NORMAL)
    public void testGetNonExistentUser() {
        logger.info("Testing get non-existent user");

        Response response = UserApiService.getUserById(requestSpec, 99999L);

        // Assert error status code
        Assert.assertEquals(response.getStatusCode(), 404, "Non-existent user should return 404");

        logger.info("Non-existent user request returned expected 404");
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Story("Get Users")
    @Description("Test retrieving all users")
    @Severity(SeverityLevel.NORMAL)
    public void testGetAllUsers() {
        logger.info("Testing get all users");

        Response response = UserApiService.getAllUsers(requestSpec);

        // Assert status code
        Assert.assertEquals(response.getStatusCode(), 200, "Get all users should return 200");

        // Schema validation
        SchemaValidator.validateUserListSchema(response);

        // Response validation
        List<User> users = response.jsonPath().getList("$", User.class);
        Assert.assertNotNull(users, "Users list should not be null");

        logger.info("Retrieved " + users.size() + " users");
    }

    @Test(dataProvider = "validUserData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Update User")
    @Description("Test updating user information")
    @Severity(SeverityLevel.NORMAL)
    public void testUpdateUser(UserDto userDto) {
        logger.info("Testing user update");

        // First create a user
        Response createResponse = UserApiService.createUser(requestSpec, userDto);
        User createdUser = createResponse.as(User.class);

        // Update user data
        UserDto updateDto = UserDto.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .password("newPassword123")
                .fullName("Updated" + userDto.getFullName())
                .phoneNumber(userDto.getPhoneNumber())
                .build();

        // Update the user
        Response updateResponse = UserApiService.updateUser(requestSpec, createdUser.getId(), updateDto);

        // Assert status code
        Assert.assertEquals(updateResponse.getStatusCode(), 200, "Update user should return 200");

        // Schema validation
        SchemaValidator.validateUserSchema(updateResponse);

        // Response validation
        User updatedUser = updateResponse.as(User.class);
        Assert.assertEquals(updatedUser.getId(), createdUser.getId(), "User ID should remain same");
        Assert.assertEquals(updatedUser.getFullName(), updateDto.getFullName(), "Full name should be updated");

        logger.info("User updated successfully: " + updatedUser.getId());
    }

    @Test(dataProvider = "validUserData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Delete User")
    @Description("Test deleting a user")
    @Severity(SeverityLevel.CRITICAL)
    public void testDeleteUser(UserDto userDto) {
        logger.info("Testing user deletion");

        // First create a user
        Response createResponse = UserApiService.createUser(requestSpec, userDto);
        User createdUser = createResponse.as(User.class);

        // Delete the user
        Response deleteResponse = UserApiService.deleteUser(requestSpec, createdUser.getId());

        // Assert status code
        Assert.assertEquals(deleteResponse.getStatusCode(), 204, "Delete user should return 204");

        // Verify user is deleted by trying to retrieve it
        Response getResponse = UserApiService.getUserById(requestSpec, createdUser.getId());
        Assert.assertEquals(getResponse.getStatusCode(), 404, "Deleted user should return 404");

        logger.info("User deleted successfully: " + createdUser.getId());
    }

    @Test(dataProvider = "validUserData", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Get User by Username")
    @Description("Test retrieving user by username")
    @Severity(SeverityLevel.NORMAL)
    public void testGetUserByUsername(UserDto userDto) {
        logger.info("Testing get user by username");

        // First create a user
        Response createResponse = UserApiService.createUser(requestSpec, userDto);
        User createdUser = createResponse.as(User.class);

        // Then retrieve by username
        Response getResponse = UserApiService.getUserByUsername(requestSpec, createdUser.getUsername());

        // Assert status code
        Assert.assertEquals(getResponse.getStatusCode(), 200, "Get user by username should return 200");

        // Schema validation
        SchemaValidator.validateUserSchema(getResponse);

        // Response validation
        User retrievedUser = getResponse.as(User.class);
        Assert.assertEquals(retrievedUser.getUsername(), createdUser.getUsername(), "Username should match");
        Assert.assertEquals(retrievedUser.getId(), createdUser.getId(), "User ID should match");

        logger.info("User retrieved by username successfully: " + retrievedUser.getUsername());
    }

    @Test(dataProvider = "userDataFromExcel", dataProviderClass = DataProviders.class,
            retryAnalyzer = RetryAnalyzer.class)
    @Story("Create User from Excel")
    @Description("Test creating users with data from Excel")
    @Severity(SeverityLevel.NORMAL)
    public void testCreateUserFromExcel(Map<String, String> userData) {
        logger.info("Testing user creation from Excel data");

        UserDto userDto = UserDto.builder()
                .username(userData.get("username"))
                .email(userData.get("email"))
                .password(userData.get("password"))
                .fullName(userData.get("fullName"))
                .phoneNumber(userData.get("phoneNumber"))
                .build();

        Response response = UserApiService.createUser(requestSpec, userDto);

        // Assert status code
        Assert.assertEquals(response.getStatusCode(), 201, "User creation should return 201");

        // Schema validation
        SchemaValidator.validateUserSchema(response);

        User createdUser = response.as(User.class);
        Assert.assertEquals(createdUser.getUsername(), userDto.getUsername(), "Username should match Excel data");

        logger.info("User created from Excel data successfully: " + createdUser.getId());
    }
}