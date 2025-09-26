package org.banking.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("accountType")
    private String accountType;

    @JsonProperty("balance")
    private BigDecimal balance;

    @JsonProperty("currency")
    private String currency;

    @JsonIgnore
    private Long userId;

    @JsonProperty("userId")
    public Long getUserId() {
        if (user != null) {
            return user.getId();
        }
        return userId;
    }

    @JsonProperty("userId")
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @JsonProperty("user")
    private User user;

    @JsonProperty("createdAt")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updatedAt;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("overdraftLimit")
    private BigDecimal overdraftLimit;

    @JsonProperty("minimumBalance")
    private BigDecimal minimumBalance;

    @JsonProperty("interestRate")
    private BigDecimal interestRate;

    @JsonProperty("accountStatus")
    private String accountStatus;

    @JsonProperty("branch")
    private String branch;

    @JsonProperty("accountDescription")
    private String accountDescription;

    @JsonProperty("lastTransactionDate")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime lastTransactionDate;

    @JsonProperty("monthlyFee")
    private BigDecimal monthlyFee;

    @JsonProperty("transactionLimit")
    private BigDecimal transactionLimit;

    @JsonProperty("dailyTransactionLimit")
    private BigDecimal dailyTransactionLimit;

    @JsonProperty("frozen")
    private Boolean frozen;

    @JsonProperty("closedDate")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime closedDate;

    // Business logic helper methods
    public boolean isActive() {
        return active != null && active && !"CLOSED".equals(accountStatus);
    }

    public boolean isFrozen() {
        return frozen != null && frozen;
    }

    public boolean hasOverdraftLimit() {
        return overdraftLimit != null && overdraftLimit.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getAvailableBalance() {
        if (balance == null) {
            return BigDecimal.ZERO;
        }

        if (hasOverdraftLimit()) {
            return balance.add(overdraftLimit);
        }

        return balance;
    }

    public boolean hasSufficientBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        return getAvailableBalance().compareTo(amount) >= 0;
    }

    public boolean isMinimumBalanceViolated() {
        if (minimumBalance == null || balance == null) {
            return false;
        }

        return balance.compareTo(minimumBalance) < 0;
    }

    public String getAccountTypeDisplayName() {
        if (accountType == null) {
            return "Unknown";
        }

        switch (accountType.toUpperCase()) {
            case "SAVINGS":
                return "Savings Account";
            case "CHECKING":
                return "Checking Account";
            case "BUSINESS":
                return "Business Account";
            case "PREMIUM_SAVINGS":
                return "Premium Savings Account";
            default:
                return accountType;
        }
    }

    public boolean isBusinessAccount() {
        return "BUSINESS".equals(accountType);
    }

    public boolean isSavingsAccount() {
        return "SAVINGS".equals(accountType) || "PREMIUM_SAVINGS".equals(accountType);
    }

    public boolean isCheckingAccount() {
        return "CHECKING".equals(accountType);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", accountType='" + accountType + '\'' +
                ", balance=" + balance +
                ", currency='" + currency + '\'' +
                ", userId=" + userId +
                ", active=" + active +
                ", overdraftLimit=" + overdraftLimit +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return id != null && id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}