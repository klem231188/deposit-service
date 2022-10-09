package com.glady.deposit.controller;

import com.glady.deposit.model.contract.Balance;
import com.glady.deposit.model.contract.Deposit;
import com.glady.deposit.model.contract.User;
import com.glady.deposit.service.BalanceService;
import com.glady.deposit.service.DepositService;
import com.glady.deposit.service.UserService;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/deposits")
@RequiredArgsConstructor
@Validated
public class DepositController {

    private final DepositService depositService;

    private final UserService userService;

    private final BalanceService balanceService;

    private final Object lock = new Object();

    private final Validator validator;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The deposit has been created successfully", content = {@Content(schema = @Schema(implementation = Deposit.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "415", description = "Invalid media type", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<Deposit> createDeposit(
            @RequestBody Deposit deposit,
            Principal principal
    ) {
        // Check that input is correct
        // We don't use @Valid on the deposit on purpose, otherwise it could throw a <MethodArgumentNotValidException>
        Set<ConstraintViolation<Deposit>> constraints = validator.validate(deposit);
        if (!constraints.isEmpty()) {
            throw new ConstraintViolationException(constraints);
        }

        // To simplify, let's say that principal name is the customer id
        // It's an uuid more precisely
        String customerId = principal.getName();

        // Verify that the user belongs to the company
        // We want to prevent company A to create a deposit for a company B user
        verifyUser(deposit.getUserId(), customerId);

        // We use synchronized to avoid a negative balance in case of concurrent calls
        // This can be improved by using a specific lock per customerId
        synchronized (lock) {
            // Verify that the company balance is greater than the deposit amount
            Balance balance = balanceService.getBalance(customerId);
            if (balance == null || balance.getAmount() < deposit.getAmount()) {
                String error = String.format("Balance of customer %s has not been found or is insufficient", customerId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, error);
            }

            // Create the deposit
            Deposit createdDeposit = depositService.createDeposit(deposit);

            // Update the balance
            balanceService.decrease(balance, deposit.getAmount());

            // Return response
            return new ResponseEntity<>(createdDeposit, HttpStatus.CREATED);
        }
    }

    @GetMapping(
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The deposits found", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = Deposit.class)))}),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "415", description = "Invalid media type", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<List<Deposit>> getDeposits(
            @RequestParam @NotBlank @Size(max = 50) String userId,
            Principal principal
    ) {
        // Check that params are correct
        // Nothing to do, it's done thanks to @Validated annotation class

        // To simplify, let's say that principal name is the customer id
        // It's an uuid more precisely
        String customerId = principal.getName();

        // Verify that the user belongs to the company
        // We want to prevent company A to get deposits for a company B user
        verifyUser(userId, customerId);

        // Get the deposits
        List<Deposit> deposits = depositService.getDeposits(userId);

        // Return response
        return new ResponseEntity<>(deposits, HttpStatus.OK);
    }

    private void verifyUser(
            String userId,
            String customerId
    ) {
        User user = userService.getUser(userId);
        if (user == null || !user.getCustomerId().equals(customerId)) {
            String error = String.format("User %s has not been found or doesn't belong to customer %s", userId, customerId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, error);
        }
    }
}
