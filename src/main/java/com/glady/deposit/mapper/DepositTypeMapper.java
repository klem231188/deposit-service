package com.glady.deposit.mapper;


import com.glady.deposit.model.contract.DepositType;

public class DepositTypeMapper {

    public static com.glady.deposit.model.db.DepositType toEntity(
            DepositType contractDepositType
    ) {
        return com.glady.deposit.model.db.DepositType.valueOf(contractDepositType.toString());
    }

    public static DepositType fromEntity(
            com.glady.deposit.model.db.DepositType dbDepositType
    ) {
        return DepositType.valueOf(dbDepositType.toString());
    }
}
