package com.glady.deposit.model.contract;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class Deposit {
    private String id;

    @NotBlank
    @Size(max = 50)
    private String userId;

    @NotNull
    private DepositType depositType;

    @Positive
    private double amount;

    private LocalDate expirationDate;
}
