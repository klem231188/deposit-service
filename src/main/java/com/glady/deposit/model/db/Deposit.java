package com.glady.deposit.model.db;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Deposit {
    private int id;
    private String uuid;
    private String userId;
    private DepositType depositType;
    private double amount;
    private LocalDate expirationDate;
}
