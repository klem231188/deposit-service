package com.glady.deposit.repository;

import com.glady.deposit.model.db.Deposit;
import com.glady.deposit.model.db.DepositType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DepositRowMapper implements RowMapper<Deposit> {

    @Override
    public Deposit mapRow(
            ResultSet rs,
            int rowNum
    ) throws SQLException {
        Deposit deposit = new Deposit();

        deposit.setId(rs.getInt("id"));
        deposit.setUserId(rs.getString("userId"));
        deposit.setUuid(rs.getString("uuid"));
        deposit.setDepositType(DepositType.fromId(rs.getInt("depositType")));
        deposit.setAmount(rs.getDouble("amount"));
        deposit.setExpirationDate(rs.getDate("expirationDate").toLocalDate());

        return deposit;
    }
}
