/*
 * Author: Shady Ahmed
 * Date: 2025-09-27
 * Project: Mobile Banking API Testing using RestAssured (E2E)
 * My Linked-in: https://www.linkedin.com/in/shady-ahmed97/.
 */
package org.banking.pojo;

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
public class Transaction {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("transactionReference")
    private String transactionReference;

    @JsonProperty("transactionType")
    private String transactionType;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("description")
    private String description;

    @JsonProperty("fromAccountId")
    private Long fromAccountId;

    @JsonProperty("toAccountId")
    private Long toAccountId;

    @JsonProperty("fromAccount")
    private Account fromAccount;

    @JsonProperty("toAccount")
    private Account toAccount;

    @JsonProperty("fromAccountId")
    public Long getFromAccountId() {
        if (fromAccount != null) {
            return fromAccount.getId();
        }
        return null;
    }

    @JsonProperty("getToAccountId")
    public Long getToAccountId() {
        if (toAccount != null) {
            return toAccount.getId();
        }
        return null;
    }

    @JsonProperty("status")
    private String status;

    @JsonProperty("createdAt")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;

    @JsonProperty("completedAt")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime completedAt;

    @JsonProperty("fee")
    private BigDecimal fee;

    @JsonProperty("exchangeRate")
    private BigDecimal exchangeRate;

    @JsonProperty("originalAmount")
    private BigDecimal originalAmount;

    @JsonProperty("originalCurrency")
    private String originalCurrency;

    @JsonProperty("balanceAfterTransaction")
    private BigDecimal balanceAfterTransaction;

    @JsonProperty("transactionCategory")
    private String transactionCategory;

    @JsonProperty("merchantName")
    private String merchantName;

    @JsonProperty("merchantCategory")
    private String merchantCategory;

    @JsonProperty("location")
    private String location;

    @JsonProperty("approvedBy")
    private String approvedBy;

    @JsonProperty("rejectionReason")
    private String rejectionReason;

    @JsonProperty("reversalTransactionId")
    private Long reversalTransactionId;

    @JsonProperty("parentTransactionId")
    private Long parentTransactionId;

    @JsonProperty("batchId")
    private String batchId;

    @JsonProperty("channel")
    private String channel;

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("ipAddress")
    private String ipAddress;

    @JsonProperty("tags")
    private String tags;

    @JsonProperty("metadata")
    private String metadata;

    @JsonProperty("scheduledDate")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime scheduledDate;

    @JsonProperty("recurringTransactionId")
    private Long recurringTransactionId;

    @JsonProperty("authorizationCode")
    private String authorizationCode;

    @JsonProperty("cleared")
    private Boolean cleared;

    @JsonProperty("clearedAt")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime clearedAt;

    // Business logic helper methods
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public boolean isSuccessful() {
        return isCompleted() || isCleared();
    }

    public boolean isCleared() {
        return cleared != null && cleared;
    }

    public boolean isDebit() {
        return "WITHDRAWAL".equals(transactionType) ||
                ("TRANSFER".equals(transactionType) && fromAccountId != null);
    }

    public boolean isCredit() {
        return "DEPOSIT".equals(transactionType) ||
                ("TRANSFER".equals(transactionType) && toAccountId != null);
    }

    public boolean isTransfer() {
        return "TRANSFER".equals(transactionType);
    }

    public boolean isDeposit() {
        return "DEPOSIT".equals(transactionType);
    }

    public boolean isWithdrawal() {
        return "WITHDRAWAL".equals(transactionType);
    }

    public boolean hasFee() {
        return fee != null && fee.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getTotalAmount() {
        if (amount == null) {
            return BigDecimal.ZERO;
        }

        if (hasFee()) {
            return amount.add(fee);
        }

        return amount;
    }

    public boolean isCrossCurrency() {
        return originalCurrency != null && !originalCurrency.equals(currency);
    }

    public boolean isScheduled() {
        return scheduledDate != null && scheduledDate.isAfter(LocalDateTime.now());
    }

    public boolean isRecurring() {
        return recurringTransactionId != null;
    }

    public boolean isReversal() {
        return reversalTransactionId != null;
    }

    public boolean isBatchTransaction() {
        return batchId != null && !batchId.trim().isEmpty();
    }

    public String getTransactionTypeDisplayName() {
        if (transactionType == null) {
            return "Unknown";
        }

        switch (transactionType.toUpperCase()) {
            case "DEPOSIT":
                return "Deposit";
            case "WITHDRAWAL":
                return "Withdrawal";
            case "TRANSFER":
                return "Transfer";
            case "PAYMENT":
                return "Payment";
            case "REFUND":
                return "Refund";
            case "FEE":
                return "Fee";
            case "INTEREST":
                return "Interest";
            default:
                return transactionType;
        }
    }

    public String getStatusDisplayName() {
        if (status == null) {
            return "Unknown";
        }

        switch (status.toUpperCase()) {
            case "PENDING":
                return "Pending";
            case "COMPLETED":
                return "Completed";
            case "FAILED":
                return "Failed";
            case "CANCELLED":
                return "Cancelled";
            case "PROCESSING":
                return "Processing";
            case "AUTHORIZED":
                return "Authorized";
            case "DECLINED":
                return "Declined";
            default:
                return status;
        }
    }

    public String getChannelDisplayName() {
        if (channel == null) {
            return "Unknown";
        }

        switch (channel.toUpperCase()) {
            case "ATM":
                return "ATM";
            case "ONLINE":
                return "Online Banking";
            case "MOBILE":
                return "Mobile App";
            case "BRANCH":
                return "Branch";
            case "POS":
                return "Point of Sale";
            case "API":
                return "API";
            default:
                return channel;
        }
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", transactionReference='" + transactionReference + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status='" + status + '\'' +
                ", fromAccountId=" + fromAccountId +
                ", toAccountId=" + toAccountId +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}