package com.glady.deposit.service;

import com.glady.deposit.mapper.DepositMapper;
import com.glady.deposit.model.contract.Deposit;
import com.glady.deposit.repository.DepositRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final DepositRepository depositRepository;

    private final Clock clock;

    public Deposit createDeposit(
            Deposit deposit
    ) {
        // Build the "Entity" Deposit bean
        com.glady.deposit.model.db.Deposit dbDeposit = DepositMapper.toEntity(deposit);

        LocalDate expirationDate = switch (dbDeposit.getDepositType()) {
            case GIFT -> getGiftExpirationDate();
            case MEAL -> getMealExpirationDate();
        };
        dbDeposit.setExpirationDate(expirationDate);

        String uuid = UUID.randomUUID().toString();
        dbDeposit.setUuid(uuid);

        // Insert the "Entity" Deposit bean in database
        depositRepository.createDeposit(dbDeposit);

        // Return the "Contract" Deposit bean
        return DepositMapper.fromEntity(dbDeposit);
    }

    public List<Deposit> getDeposits(
            String userId
    ) {
        // Get the "Entity" Deposit beans from database
        List<com.glady.deposit.model.db.Deposit> dbDeposits = depositRepository.getDeposits(userId);

        // Return the "Contract" Deposit beans
        return  dbDeposits.stream()
                .map(DepositMapper::fromEntity)
                .collect(Collectors.toList());
    }

    public LocalDate getGiftExpirationDate(
    ) {
        // Rule is not clear to me, is it 365 or 364 days ?
        // According to unit test it's more likely to be 364 days
        return LocalDate.now(clock).plusDays(364);
    }

    public LocalDate getMealExpirationDate(
    ) {
        LocalDate expirationDate = LocalDate.now(clock).plusYears(1).withMonth(Month.FEBRUARY.getValue());
        return expirationDate.withDayOfMonth(expirationDate.getMonth().length(expirationDate.isLeapYear()));
    }
}
