package com.glady.deposit.model.db;

import java.util.Arrays;

public enum DepositType {
    GIFT(0),
    MEAL(1);

    private final int id;

    DepositType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static DepositType fromId(
            int id
    ) {
        return Arrays.stream(DepositType.values())
                .filter(depositType -> depositType.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
