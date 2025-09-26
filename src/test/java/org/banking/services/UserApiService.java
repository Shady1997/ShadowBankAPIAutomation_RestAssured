package org.banking.services;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.banking.dto.UserDto;
import org.banking.pojo.User;

import static io.restassured.RestAssured.given;

public class UserApiService {

    private static final Logger logger = LogManager.getLogger(UserApiService.class);
    private static final String USERS_ENDPOINT = "/users";

    @Step("Create new user")
    public static Response createUser(RequestSpecification requestSpec, UserDto userDto) {
        logger.info("Creating user with username: " + userDto.getUsername());

        Response response = requestSpec
                .body(userDto)
                .when()
                .post(USERS_ENDPOINT)
                .then()
                .extract()
                .response();

        logger.info("User creation response status: " + response.getStatusCode());
        return response;
    }

    @Step("Get user by ID: {userId}")
    public static Response getUserById(RequestSpecification requestSpec, Long userId) {
        logger.info("Getting user by ID: " + userId);

        Response response = requestSpec
                .when()
                .get(USERS_ENDPOINT + "/" + userId)
                .then()
                .extract()
                .response();

        logger.info("Get user by ID response status: " + response.getStatusCode());
        return response;
    }

    @Step("Get all users")
    public static Response getAllUsers(RequestSpecification requestSpec) {
        logger.info("Getting all users");

        Response response = requestSpec
                .when()
                .get(USERS_ENDPOINT)
                .then()
                .extract()
                .response();

        logger.info("Get all users response status: " + response.getStatusCode());
        return response;
    }

    @Step("Update user with ID: {userId}")
    public static Response updateUser(RequestSpecification requestSpec, Long userId, UserDto userDto) {
        logger.info("Updating user with ID: " + userId);

        Response response = requestSpec
                .body(userDto)
                .when()
                .put(USERS_ENDPOINT + "/" + userId)
                .then()
                .extract()
                .response();

        logger.info("Update user response status: " + response.getStatusCode());
        return response;
    }

    @Step("Delete user with ID: {userId}")
    public static Response deleteUser(RequestSpecification requestSpec, Long userId) {
        logger.info("Deleting user with ID: " + userId);

        Response response = requestSpec
                .when()
                .delete(USERS_ENDPOINT + "/" + userId)
                .then()
                .extract()
                .response();

        logger.info("Delete user response status: " + response.getStatusCode());
        return response;
    }

    @Step("Get user by username: {username}")
    public static Response getUserByUsername(RequestSpecification requestSpec, String username) {
        logger.info("Getting user by username: " + username);

        Response response = requestSpec
                .when()
                .get(USERS_ENDPOINT + "/username/" + username)
                .then()
                .extract()
                .response();

        logger.info("Get user by username response status: " + response.getStatusCode());
        return response;
    }
}