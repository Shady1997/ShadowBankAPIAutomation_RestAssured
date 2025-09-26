package org.banking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    @JsonProperty("accountType")
    private String accountType;

    @JsonProperty("status")
    private String status;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("balance")
    private BigDecimal balance;

    @JsonProperty("creditLimit")
    private BigDecimal creditLimit;
}