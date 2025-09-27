/*
 * Author: Shady Ahmed
 * Date: 2025-09-27
 * Project: Mobile Banking API Testing using RestAssured (E2E)
 * My Linked-in: https://www.linkedin.com/in/shady-ahmed97/.
 */
package org.banking.dataproviders;

import com.github.javafaker.Faker;
import org.banking.dto.AccountDto;
import org.banking.dto.TransactionDto;
import org.banking.dto.UserDto;
import org.banking.utils.ExcelDataReader;
import org.banking.utils.JsonDataReader;
import org.testng.annotations.DataProvider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataProviders {

    private static final Faker faker = new Faker();

    @DataProvider(name = "validUserData")
    public static Object[][] validUserData() {
        return new Object[][]{
                {createValidUserDto()}
        };
    }

    @DataProvider(name = "invalidUserData")
    public static Object[][] invalidUserData() {
        return new Object[][]{
                {UserDto.builder().username("").email("invalid-email").build()},
                {UserDto.builder().username("ab").email("test@test.com").build()}, // username too short
                {UserDto.builder().username("validuser").email("").build()}, // empty email
                {UserDto.builder().username(null).email("test@test.com").build()}, // null username
        };
    }

    @DataProvider(name = "validAccountData")
    public static Object[][] validAccountData() {
        return new Object[][]{
                {createValidAccountDto()}
        };
    }

    @DataProvider(name = "invalidAccountData")
    public static Object[][] invalidAccountData() {
        return new Object[][]{
                {AccountDto.builder().accountType("").userId(1L).build()} // empty account type
//                ,{AccountDto.builder().accountType("SAVINGS").userId(null).build()}, // null user ID
//                {AccountDto.builder().accountType("INVALID_TYPE").userId(1L).build()}, // invalid account type
        };
    }

    @DataProvider(name = "validTransactionData")
    public static Object[][] validTransactionData() {
        return new Object[][]{
                {createValidTransactionDto()}
//                ,{createValidTransactionDto()},
//                {createValidTransactionDto()}
        };
    }

    @DataProvider(name = "invalidTransactionData")
    public static Object[][] invalidTransactionData() {
        return new Object[][]{
                {TransactionDto.builder().amount(BigDecimal.ZERO).build()}, // zero amount
                {TransactionDto.builder().amount(new BigDecimal("-100")).build()}, // negative amount
                {TransactionDto.builder().transactionType("").amount(new BigDecimal("100")).build()}, // empty type
        };
    }

    @DataProvider(name = "userDataFromJson")
    public static Object[][] userDataFromJson() {
        List<UserDto> users = JsonDataReader.readTestDataList("user-test-data.json", "validUsers", UserDto.class);
        Object[][] data = new Object[users.size()][1];
        for (int i = 0; i < users.size(); i++) {
            data[i][0] = users.get(i);
        }
        return data;
    }

    @DataProvider(name = "userDataFromExcel")
    public static Object[][] userDataFromExcel() {
        return ExcelDataReader.readExcelData("user-test-data.xlsx", "ValidUsers");
    }

    @DataProvider(name = "accountDataFromExcel")
    public static Object[][] accountDataFromExcel() {
        return ExcelDataReader.readExcelData("account-test-data.xlsx", "ValidAccounts");
    }

    @DataProvider(name = "transactionDataFromExcel")
    public static Object[][] transactionDataFromExcel() {
        return ExcelDataReader.readExcelData("transaction-test-data.xlsx", "ValidTransactions");
    }

    @DataProvider(name = "e2eTestData")
    public static Object[][] e2eTestData() {
        List<Object[]> testData = new ArrayList<>();

        // Scenario 1: Create user, account, and perform transaction
        UserDto user1 = createValidUserDto();
        AccountDto account1 = createValidAccountDto();
        TransactionDto transaction1 = createValidTransactionDto();
        testData.add(new Object[]{user1, account1, transaction1, "E2E_Scenario_1"});

        // Scenario 2: Different account types and transaction types
        UserDto user2 = createValidUserDto();
        AccountDto account2 = AccountDto.builder()
                .accountType("CHECKING")
                .balance(new BigDecimal("2000.00"))
                .creditLimit(new BigDecimal("500.00"))
                .build();
        TransactionDto transaction2 = TransactionDto.builder()
                .transactionType("WITHDRAWAL")
                .amount(new BigDecimal("300.00"))
                .currency("EUR")
                .description("ATM Withdrawal")
                .build();
        testData.add(new Object[]{user2, account2, transaction2, "E2E_Scenario_2"});

        return testData.toArray(new Object[testData.size()][]);
    }

    public static UserDto createValidUserDto() {
        return UserDto.builder()
                .username(faker.name().username())
                .email(faker.internet().emailAddress())
                .password(faker.internet().password(8, 20))
                .phoneNumber("+1234567890")
                .fullName(faker.name().fullName())
                .build();
    }

    public static AccountDto createValidAccountDto() {
        return AccountDto.builder()
                .accountType(faker.options().option("CREDIT", "CHECKING", "SAVINGS"))
                .status(faker.options().option("ACTIVE", "INACTIVE"))
                .balance(new BigDecimal(faker.number().numberBetween(100, 10000)))
                .creditLimit(new BigDecimal(faker.number().numberBetween(0, 1000)))
                .build();
    }

    private static TransactionDto createValidTransactionDto() {
        return TransactionDto.builder()
                .transactionType(faker.options().option("DEPOSIT", "WITHDRAWAL", "TRANSFER"))
                .amount(new BigDecimal(faker.number().numberBetween(10, 1000)))
                .currency(faker.options().option("USD", "EUR", "GBP"))
                .description(faker.lorem().sentence())
                .build();
    }
}