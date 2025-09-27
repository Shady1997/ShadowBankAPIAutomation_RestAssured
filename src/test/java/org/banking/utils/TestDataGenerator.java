/*
 * Author: Shady Ahmed
 * Date: 2025-09-27
 * Project: Mobile Banking API Testing using RestAssured (E2E)
 * My Linked-in: https://www.linkedin.com/in/shady-ahmed97/.
 */
// TestDataGenerator.java
package org.banking.utils;

import com.github.javafaker.Faker;
import org.banking.dto.AccountDto;
import org.banking.dto.TransactionDto;
import org.banking.dto.UserDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestDataGenerator {

    private static final Faker faker = new Faker(Locale.US);
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // User data generators
    public static UserDto generateValidUser() {
        return UserDto.builder()
                .username(faker.name().username())
                .email(faker.internet().emailAddress())
                .password(generateSecurePassword())
                .fullName(faker.name().fullName())
                .phoneNumber(faker.phoneNumber().phoneNumber())
                .build();
    }

    public static UserDto generateUserWithSpecificData(String username, String email) {
        return UserDto.builder()
                .username(username)
                .email(email)
                .password(generateSecurePassword())
                .fullName(faker.name().fullName())
                .phoneNumber(faker.phoneNumber().phoneNumber())
                .build();
    }

    public static List<UserDto> generateMultipleUsers(int count) {
        List<UserDto> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(generateValidUser());
        }
        return users;
    }

    // Account data generators
    public static AccountDto generateValidAccount() {
        return AccountDto.builder()
                .accountType(faker.options().option("SAVINGS", "CHECKING", "BUSINESS"))
//                .currency(faker.options().option("USD", "EUR", "GBP"))
                .balance(generateRandomBalance())
                .creditLimit(generateRandomOverdraftLimit())
                .build();
    }

    public static AccountDto generateSavingsAccount() {
        return AccountDto.builder()
                .accountType("SAVINGS")
                .balance(new BigDecimal(faker.number().numberBetween(500, 5000)))
                .creditLimit(BigDecimal.ZERO)
                .build();
    }

    public static AccountDto generateCheckingAccount() {
        return AccountDto.builder()
                .accountType("CHECKING")
                .balance(new BigDecimal(faker.number().numberBetween(100, 2000)))
                .creditLimit(new BigDecimal(faker.number().numberBetween(100, 1000)))
                .build();
    }

    public static AccountDto generateBusinessAccount() {
        return AccountDto.builder()
                .accountType("BUSINESS")
                .balance(new BigDecimal(faker.number().numberBetween(1000, 50000)))
                .creditLimit(new BigDecimal(faker.number().numberBetween(1000, 10000)))
                .build();
    }

    // Transaction data generators
    public static TransactionDto generateValidTransaction() {
        return TransactionDto.builder()
                .transactionType(faker.options().option("DEPOSIT", "WITHDRAWAL", "TRANSFER"))
                .amount(new BigDecimal(faker.number().numberBetween(10, 1000)))
                .currency("USD")
                .description(faker.lorem().sentence())
                .build();
    }

    public static TransactionDto generateDepositTransaction(BigDecimal amount) {
        return TransactionDto.builder()
                .transactionType("DEPOSIT")
                .amount(amount)
                .currency("USD")
                .description("Test deposit transaction")
                .build();
    }

    public static TransactionDto generateWithdrawalTransaction(BigDecimal amount) {
        return TransactionDto.builder()
                .transactionType("WITHDRAWAL")
                .amount(amount)
                .currency("USD")
                .description("Test withdrawal transaction")
                .build();
    }

    public static TransactionDto generateTransferTransaction(BigDecimal amount) {
        return TransactionDto.builder()
                .transactionType("TRANSFER")
                .amount(amount)
                .currency("USD")
                .description("Test transfer transaction")
                .build();
    }

    // Helper methods
    private static String generateSecurePassword() {
        return faker.internet().password(8, 16, true, true, true);
    }

    private static String generateRandomDateOfBirth() {
        LocalDate birthDate = LocalDate.now().minusYears(faker.number().numberBetween(18, 80));
        return birthDate.format(dateFormatter);
    }

    private static BigDecimal generateRandomBalance() {
        return new BigDecimal(faker.number().numberBetween(100, 10000));
    }

    private static BigDecimal generateRandomOverdraftLimit() {
        return new BigDecimal(faker.number().numberBetween(0, 2000));
    }

    // Invalid data generators for negative testing
    public static UserDto generateInvalidUserEmptyFields() {
        return UserDto.builder()
                .username("")
                .email("")
                .password("")
                .fullName("")
                .build();
    }

    public static UserDto generateInvalidUserWrongEmail() {
        return UserDto.builder()
                .username(faker.name().username())
                .email("invalid-email-format")
                .password(generateSecurePassword())
                .fullName(faker.name().fullName())
                .build();
    }

    public static UserDto generateInvalidUserShortUsername() {
        return UserDto.builder()
                .username("ab") // Too short
                .email(faker.internet().emailAddress())
                .password(generateSecurePassword())
                .fullName(faker.name().fullName())
                .build();
    }

    public static AccountDto generateInvalidAccountEmptyType() {
        return AccountDto.builder()
                .accountType("")
                .balance(new BigDecimal("1000"))
                .build();
    }

    public static TransactionDto generateInvalidTransactionZeroAmount() {
        return TransactionDto.builder()
                .transactionType("DEPOSIT")
                .amount(BigDecimal.ZERO)
                .currency("USD")
                .description("Invalid zero amount transaction")
                .build();
    }

    public static TransactionDto generateInvalidTransactionNegativeAmount() {
        return TransactionDto.builder()
                .transactionType("WITHDRAWAL")
                .amount(new BigDecimal("-100"))
                .currency("USD")
                .description("Invalid negative amount transaction")
                .build();
    }
}