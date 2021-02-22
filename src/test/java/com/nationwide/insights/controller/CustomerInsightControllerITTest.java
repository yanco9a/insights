package com.nationwide.insights.controller;

import TestUtils.JsonHelper;
import com.nationwide.insights.domain.Insight;
import com.nationwide.insights.service.TransactionCategory;
import com.nationwide.insights.domain.customer.Customer;
import com.nationwide.insights.domain.customer.CustomerRepository;
import com.nationwide.insights.domain.transactions.TransactionRepository;
import com.nationwide.insights.domain.transactions.Transactions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-local.properties")
@ActiveProfiles("test")
public class CustomerInsightControllerITTest {
    private Transactions transactions;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactRepository;

    @Autowired
    private CustomerRepository customerRepository;


    @Test
    @DisplayName("GET /api/v1/customer/insights/1 when 200 OK")
    void getCustomerInsightById200Test() throws Exception {
        // Given
        Customer customer = new Customer(1L, "Louise");
        customerRepository.save(customer);
        transactions = new Transactions(
                1L,
                BigDecimal.valueOf(400000, 2),
                LocalDate.now(),
                "description",
                TransactionCategory.RESTAURANTS.toString(),
                "Nandos'",
                customer);
        transactRepository.save(transactions);

        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/v1/customer/insights/1")
                .contentType("application/json;charset=UTF-8"))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn();
        // And
        String response = result.getResponse().getContentAsString();
        List<Insight> iInsights = JsonHelper.readListFromString(response, Insight.class);
        Insight expectedInsight = new Insight("You've spent £4000 in restaurants this month");
        assertEquals(1, iInsights.size());
        assertEquals(expectedInsight, iInsights.get(0));
    }

    @Test
    @DisplayName("GET /api/v1/customer/insights/99 when 200 but customer insight does not exist")
    void getCustomerInsightByIdButDoesNotExistTest() throws Exception {
        // When
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/v1/customer/insights/99")
                .contentType("application/json;charset=UTF-8"))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string("[]"));
    }

    @Test
    @DisplayName("GET /api/v1/customer/insights/0 when 400 BAD REQUEST")
    void getCustomerInsightById400Test() throws Exception {
        // When
        String notPositiveNumeric = String.valueOf(0L);
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/v1/customer/insights/" + notPositiveNumeric)
                .contentType("application/json;charset=UTF-8"))
                // Then
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.statusCode", is(400)))
                .andExpect(jsonPath("$.message", containsString("Customer id must be greater than 0")))
                .andExpect(jsonPath("$.description", is("uri=/api/v1/customer/insights/0")))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}
