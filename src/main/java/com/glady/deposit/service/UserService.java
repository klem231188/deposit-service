package com.glady.deposit.service;

import com.glady.deposit.model.contract.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service in charge of users
 * For simplicity, everything is stored "in-memory".
 * In reality, this class should be a proxy to the real "user-service", another micro-service.
 */
@Service
public class UserService {

    private final List<User> users;

    public UserService() {
        this.users = new ArrayList<>();
        this.users.add(new User("apple-user1-uuid", "Aurore", "apple-uuid"));
        this.users.add(new User("apple-user2-uuid", "Alain", "apple-uuid"));
        this.users.add(new User("tesla-user1-uuid", "Tony", "tesla-uuid"));
        this.users.add(new User("tesla-user2-uuid", "Talia", "tesla-uuid"));
    }

    public User getUser(
            String id
    ) {
        // In reality, you would have to call a webservice. Maybe from user-service microservice ?
        return this.users
                .stream()
                .filter(user -> user.getId().equals(id))
                .findAny()
                .orElse(null);
    }
}
