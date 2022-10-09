package com.glady.deposit.service;

import com.glady.deposit.model.contract.Balance;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service in charge of balances
 * For simplicity, everything is stored "in-memory".
 * In reality, this class should be a proxy to the real "balance-service", another micro-service.
 */
@Service
public class BalanceService {

    private final List<Balance> balances;

    public BalanceService() {
        this.balances = new ArrayList<>();
        this.balances.add(new Balance("balance-apple-uuid", 2000.0, "apple-uuid"));
        this.balances.add(new Balance("balance-tesla-uuid", 1000.0, "tesla-uuid"));
    }

    public Balance getBalance(
            String customerId
    ) {
        // In reality, you would have to call a webservice. Maybe from balance-service microservice ?
        return this.balances
                .stream()
                .filter(balance -> balance.getCustomerId().equals(customerId))
                .findAny()
                .orElse(null);
    }

    public void decrease(
            Balance balance,
            double amount
    ) {
        // In reality, you would have to call a webservice. Maybe from balance-service microservice ?
        balance.setAmount(balance.getAmount() - amount);
    }
}
