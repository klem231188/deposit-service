package com.glady.deposit.model.contract;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Balance {
    private final String id;
    private double amount;
    private final String customerId;

    public Balance(
            String id,
            double amount,
            String customerId
    ) {
        this.id = id;
        this.amount = amount;
        this.customerId = customerId;
    }
}
