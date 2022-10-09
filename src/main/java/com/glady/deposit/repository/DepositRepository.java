package com.glady.deposit.repository;

import com.glady.deposit.model.db.Deposit;
import com.glady.deposit.model.db.DepositType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DepositRepository {

    private final Clock clock;
    private final DepositRowMapper depositRowMapper;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public void createDeposit(
            Deposit deposit
    ) {
        String sql = """
                 INSERT INTO Deposit (uuid, userId, depositType, amount, expirationDate)
                 VALUES (:uuid, :userId, :depositType, :amount, :expirationDate)
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("uuid", deposit.getUuid())
                .addValue("userId", deposit.getUserId())
                .addValue("depositType", deposit.getDepositType().getId())
                .addValue("amount", deposit.getAmount())
                .addValue("expirationDate", deposit.getExpirationDate());

        KeyHolder holder = new GeneratedKeyHolder();
        namedJdbcTemplate.update(sql, params, holder, new String[]{"id"});
        deposit.setId(holder.getKey().intValue());
    }

    public List<Deposit> getDeposits(
            String userId
    ) {
        String sql = """
                     SELECT * 
                     FROM Deposit 
                     WHERE userId = :userId 
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        return namedJdbcTemplate.query(sql, params, depositRowMapper);
    }
}
