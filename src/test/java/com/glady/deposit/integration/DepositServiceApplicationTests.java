package com.glady.deposit.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.glady.deposit.DepositServiceApplication;
import com.glady.deposit.model.contract.Deposit;
import com.glady.deposit.model.contract.DepositType;
import org.assertj.core.api.Assertions;
import org.assertj.db.type.Table;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.sql.DataSource;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {DepositServiceApplication.class, TestConfiguration.class})
@AutoConfigureMockMvc
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class DepositServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    public DepositServiceApplicationTests() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void shouldAddMultipleDeposits() throws Exception {
        // Test scenario :
        // 1/ Get all non expired deposits from tesla-user1-uuid --> Status OK + empty list
        // 2/ Add 100€ Gift deposit for tesla-user1-uuid --> Status CREATED
        // 3/ Add 600€ Meal deposit for tesla-user1-uuid --> Status CREATED
        // 4/ Add 600€ Meal deposit for tesla-user1-uuid --> Status BAD_REQUEST, because Tesla balance is insufficient
        // 5/ Get all non expired deposits from tesla-user1-uuid --> Status OK + 2 deposits in the list
        // 6/ Calculate the tesla-user1-uuid balance
        // 7/ Check database

        // 1/ Get all non expired deposits from tesla-user1-uuid --> Status OK + empty list
        this.mockMvc
                .perform(get("/api/v1/deposits")
                        .param("userId", "tesla-user1-uuid")
                        .with(httpBasic("tesla-uuid", "tesla-password"))
                )
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        // 2/ Add 100€ Gift deposit for tesla-user1-uuid --> Status CREATED
        Deposit giftDeposit = new Deposit();
        giftDeposit.setDepositType(DepositType.GIFT);
        giftDeposit.setAmount(100);
        giftDeposit.setUserId("tesla-user1-uuid");

        MvcResult mvcResult = this.mockMvc
                .perform(post("/api/v1/deposits")
                        .content(objectMapper.writeValueAsString(giftDeposit))
                        .header("Content-Type", "application/json")
                        .with(httpBasic("tesla-uuid", "tesla-password"))
                )
                .andExpect(status().isCreated())
                .andReturn();

        Deposit createdGiftDeposit = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Deposit.class);
        Assertions.assertThat(createdGiftDeposit.getId()).isNotBlank();
        Assertions.assertThat(createdGiftDeposit.getExpirationDate()).isEqualTo("2022-06-14");
        Assertions.assertThat(createdGiftDeposit.getAmount()).isEqualTo(giftDeposit.getAmount());
        Assertions.assertThat(createdGiftDeposit.getUserId()).isEqualTo(giftDeposit.getUserId());
        Assertions.assertThat(createdGiftDeposit.getDepositType()).isEqualTo(giftDeposit.getDepositType());

        // 3/ Add 600€ Meal deposit for tesla-user1-uuid --> Status CREATED
        Deposit mealDeposit = new Deposit();
        mealDeposit.setDepositType(DepositType.MEAL);
        mealDeposit.setAmount(600);
        mealDeposit.setUserId("tesla-user1-uuid");

        mvcResult = this.mockMvc
                .perform(post("/api/v1/deposits")
                        .content(objectMapper.writeValueAsString(mealDeposit))
                        .header("Content-Type", "application/json")
                        .with(httpBasic("tesla-uuid", "tesla-password"))
                )
                .andExpect(status().isCreated())
                .andReturn();

        Deposit createdMealDeposit = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Deposit.class);
        Assertions.assertThat(createdMealDeposit.getId()).isNotBlank();
        Assertions.assertThat(createdMealDeposit.getExpirationDate()).isEqualTo("2022-02-28");
        Assertions.assertThat(createdMealDeposit.getAmount()).isEqualTo(mealDeposit.getAmount());
        Assertions.assertThat(createdMealDeposit.getUserId()).isEqualTo(mealDeposit.getUserId());
        Assertions.assertThat(createdMealDeposit.getDepositType()).isEqualTo(mealDeposit.getDepositType());

        // 4/ Add 600€ Meal deposit for tesla-user1-uuid --> Status BAD_REQUEST,
        // Tesla balance is insufficient, because 100€ + 600€ + 600€ > 1000€
        this.mockMvc
                .perform(post("/api/v1/deposits")
                        .content(objectMapper.writeValueAsString(mealDeposit))
                        .header("Content-Type", "application/json")
                        .with(httpBasic("tesla-uuid", "tesla-password"))
                )
                .andExpect(status().isForbidden());

        // 5/ Get all non expired deposits from tesla-user1-uuid --> Status OK + 2 deposits in the list
        mvcResult = this.mockMvc
                .perform(get("/api/v1/deposits")
                        .param("userId", "tesla-user1-uuid")
                        .with(httpBasic("tesla-uuid", "tesla-password"))
                )
                .andExpect(status().isOk())
                .andReturn();

        List<Deposit> deposits = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
        });
        Assertions.assertThat(deposits).hasSize(2);
        Assertions.assertThat(deposits.get(0)).isEqualTo(createdGiftDeposit);
        Assertions.assertThat(deposits.get(1)).isEqualTo(createdMealDeposit);

        // 6/ Find the tesla-user1-uuid balance
        double balance = deposits.stream()
                .map(Deposit::getAmount)
                .reduce(0.0, Double::sum);

        Assertions.assertThat(balance).isEqualTo(700);

        // 7/ Check database
        Table table = new Table(dataSource, "Deposit");
        org.assertj.db.api.Assertions.assertThat(table)
                .hasNumberOfRows(2)
                .row(0)
                .value("id").isEqualTo(1)
                .value("uuid").isEqualTo(createdGiftDeposit.getId());
        // etc... with other columns
    }

    @Test
    public void shouldReturn401IfApiUserIsNotAuthenticated() throws Exception {
        this.mockMvc
                .perform(get("/api/v1/deposits")
                        .param("userId", "tesla-user1-uuid")
                        .with(httpBasic("tesla-uuid", "tesla-WRONG-password")) //wrong !
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturn403IfActionIsForbidden() throws Exception {
        // Test scenario :
        // 1/ Add 1500 € Gift deposit for tesla-user1-uuid --> Status FORBIDDEN (insufficient company balance)
        // 2/ Add 100 € Gift deposit for apple-user1-uuid --> Status FORBIDDEN (user belongs to apple company)
        // 3/ Get all deposits from apple-user1-uuid --> Status FORBIDDEN (user belongs to apple company)

        // 1/ Add 1500 € Gift deposit for tesla-user1-uuid --> Status FORBIDDEN (insufficient company balance)
        Deposit giftDeposit1 = new Deposit();
        giftDeposit1.setDepositType(DepositType.GIFT);
        giftDeposit1.setAmount(1500); //wrong !
        giftDeposit1.setUserId("tesla-user1-uuid");
        this.mockMvc
                .perform(post("/api/v1/deposits")
                        .content(objectMapper.writeValueAsString(giftDeposit1))
                        .header("Content-Type", "application/json")
                        .with(httpBasic("tesla-uuid", "tesla-password"))
                )
                .andExpect(status().isForbidden());

        // 2/ Add 100 € Gift deposit for apple-user1-uuid --> Status FORBIDDEN (user belongs to apple company)
        Deposit giftDeposit2 = new Deposit();
        giftDeposit2.setDepositType(DepositType.GIFT);
        giftDeposit2.setAmount(100);
        giftDeposit2.setUserId("apple-user1-uuid"); //wrong !
        this.mockMvc
                .perform(post("/api/v1/deposits")
                        .content(objectMapper.writeValueAsString(giftDeposit2))
                        .header("Content-Type", "application/json")
                        .with(httpBasic("tesla-uuid", "tesla-password"))
                )
                .andExpect(status().isForbidden());

        // 3/ Get all deposits from apple-user1-uuid --> Status FORBIDDEN (user belongs to apple company)
        this.mockMvc
                .perform(get("/api/v1/deposits")
                        .param("userId", "apple-user1-uuid") //wrong !
                        .with(httpBasic("tesla-uuid", "tesla-password"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturn400IfInputIsIncorrect() throws Exception {
        // Test scenario :
        // 1/ Create Deposit with negative amount deposit
        // 2/ Create Deposit with too long userId
        // 3/ Create Deposit with DepositType incorrect. Example GITF (instead of GIFT)
        // 4/ Get Deposit with blank userId
        // 5/ Get Deposit with too long userId

        // 1/ Create Deposit with negative amount deposit
        Deposit giftDeposit1 = new Deposit();
        giftDeposit1.setDepositType(DepositType.GIFT);
        giftDeposit1.setAmount(-1.0); //wrong !
        giftDeposit1.setUserId("tesla-user1-uuid");

        this.mockMvc
                .perform(post("/api/v1/deposits")
                        .content(objectMapper.writeValueAsString(giftDeposit1))
                        .header("Content-Type", "application/json")
                        .with(httpBasic("tesla-uuid", "tesla-password"))
                )
                .andExpect(status().isBadRequest());

        // 2/ Create Deposit with too long userId
        Deposit giftDeposit2 = new Deposit();
        giftDeposit2.setDepositType(DepositType.GIFT);
        giftDeposit2.setAmount(10);
        giftDeposit2.setUserId("very-very-very-very-very-very-very-very-very---uuid"); //wrong !

        this.mockMvc
                .perform(post("/api/v1/deposits")
                        .content(objectMapper.writeValueAsString(giftDeposit2))
                        .header("Content-Type", "application/json")
                        .with(httpBasic("tesla-uuid", "tesla-password"))
                )
                .andExpect(status().isBadRequest());

        // 3/ Create Deposit with incorrect DepositType. Example GITF (instead of GIFT)
        Deposit giftDeposit3 = new Deposit();
        giftDeposit3.setDepositType(DepositType.GIFT);
        giftDeposit3.setAmount(10);
        giftDeposit3.setUserId("tesla-user1-uuid");

        this.mockMvc
                .perform(post("/api/v1/deposits")
                        .content(objectMapper.writeValueAsString(giftDeposit3).replace("GIFT", "GITF")) //wrong !
                        .header("Content-Type", "application/json")
                        .with(httpBasic("tesla-uuid", "tesla-password"))
                )
                .andExpect(status().isBadRequest());

        // 4/ Get Deposit with blank userId
        this.mockMvc
                .perform(get("/api/v1/deposits")
                        .param("userId", "") //wrong !
                        .with(httpBasic("tesla-uuid", "tesla-password"))
                )
                .andExpect(status().isBadRequest());

        // 5/ Get Deposit with too long userId
        this.mockMvc
                .perform(get("/api/v1/deposits")
                        .param("userId", "very-very-very-very-very-very-very-very-very---uuid") //wrong !
                        .with(httpBasic("tesla-uuid", "tesla-password"))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn415IfMediaTypeIsIncorrect() throws Exception {
        // 1/ Create Deposit with wrong MediaType
        Deposit giftDeposit1 = new Deposit();
        giftDeposit1.setDepositType(DepositType.GIFT);
        giftDeposit1.setAmount(-1.0);
        giftDeposit1.setUserId("tesla-user1-uuid");

        this.mockMvc
                .perform(post("/api/v1/deposits")
                        .content(objectMapper.writeValueAsString(giftDeposit1))
                        .header("Content-Type", "application/xml") //wrong !
                        .with(httpBasic("tesla-uuid", "tesla-password"))
                )
                .andExpect(status().isUnsupportedMediaType());
    }
}
