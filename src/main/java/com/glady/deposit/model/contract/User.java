package com.glady.deposit.model.contract;

import lombok.Data;
import lombok.Getter;


@Data
public class User {
    private final String id;
    private final String name;
    private final String customerId;

    public User(
            String id,
            String name,
            String customerId
    ) {
        this.id = id;
        this.name = name;
        this.customerId = customerId;
    }
}
