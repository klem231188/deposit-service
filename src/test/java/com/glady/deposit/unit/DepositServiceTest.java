package com.glady.deposit.unit;

import com.glady.deposit.service.DepositService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class DepositServiceTest {

    private DepositService depositService;

    @Test
    public void giftExpirationDate_2021_06_15() {
        // From document :
        // John receives a Gift distribution with the amount of $100 euros from Tesla. he will therefore have $100 in gift cards in his account.
        // He received it on 06/15/2021. The gift distribution will expire on 06/14/2022.
        Instant today = Instant.parse("2021-06-15T10:15:30.00+02:00");
        Clock clock = Clock.fixed(today, ZoneId.of("Europe/Paris"));

        depositService = new DepositService(null, clock);
        LocalDate giftExpirationDate = depositService.getGiftExpirationDate();

        Assertions.assertThat(giftExpirationDate).isEqualTo("2022-06-14");
    }

    @Test
    public void mealExpirationDate_2022_01_01() {
        // From document :
        // Jessica receives a Meal distribution from Apple with the amount of $50 on 01/01/2020, the distribution ends on 02/28/2021.
        Instant today = Instant.parse("2020-01-01T10:15:30.00+02:00");
        Clock clock = Clock.fixed(today, ZoneId.of("Europe/Paris"));

        depositService = new DepositService(null, clock);
        LocalDate mealExpirationDate = depositService.getMealExpirationDate();

        Assertions.assertThat(mealExpirationDate).isEqualTo("2021-02-28");
    }

    @Test
    public void mealExpirationDate_2022_01_31() {
        Instant today = Instant.parse("2020-01-31T10:15:30.00+02:00");
        Clock clock = Clock.fixed(today, ZoneId.of("Europe/Paris"));

        depositService = new DepositService(null, clock);
        LocalDate mealExpirationDate = depositService.getMealExpirationDate();

        Assertions.assertThat(mealExpirationDate).isEqualTo("2021-02-28");
    }

    @Test
    public void mealExpirationDateOnLeapYear() {
        Instant today = Instant.parse("2023-01-31T10:15:30.00+02:00");
        Clock clock = Clock.fixed(today, ZoneId.of("Europe/Paris"));

        depositService = new DepositService(null, clock);
        LocalDate mealExpirationDate = depositService.getMealExpirationDate();

        Assertions.assertThat(mealExpirationDate).isEqualTo("2024-02-29");
    }

}
