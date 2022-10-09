package com.glady.deposit.mapper;


import com.glady.deposit.model.contract.Deposit;

public class DepositMapper {

    public static com.glady.deposit.model.db.Deposit toEntity(
            Deposit contractDeposit
    ) {
        com.glady.deposit.model.db.Deposit dbDeposit = new com.glady.deposit.model.db.Deposit();

        // Warning: the contract 'id' is voluntarily mapped on the db 'uuid'
        dbDeposit.setUuid(contractDeposit.getId());
        dbDeposit.setUserId(contractDeposit.getUserId());
        dbDeposit.setDepositType(DepositTypeMapper.toEntity(contractDeposit.getDepositType()));
        dbDeposit.setAmount(contractDeposit.getAmount());
        dbDeposit.setExpirationDate(contractDeposit.getExpirationDate());

        return dbDeposit;
    }

    public static Deposit fromEntity(
            com.glady.deposit.model.db.Deposit dbDeposit
    ) {
        Deposit contractDeposit = new Deposit();

        // Warning: the contract 'id' is voluntarily mapped on the db 'uuid'
        contractDeposit.setId(dbDeposit.getUuid());
        contractDeposit.setUserId(dbDeposit.getUserId());
        contractDeposit.setAmount(dbDeposit.getAmount());
        contractDeposit.setDepositType(DepositTypeMapper.fromEntity(dbDeposit.getDepositType()));
        contractDeposit.setExpirationDate(dbDeposit.getExpirationDate());

        return contractDeposit;
    }

}
