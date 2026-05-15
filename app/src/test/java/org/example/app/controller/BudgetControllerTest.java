package org.example.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.app.dto.*;
import org.example.app.service.BudgetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetController.class)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BudgetService budgetService;

    @MockBean
    private org.example.app.repository.UserRepository userRepository;

    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void createBudget_shouldReturn201() throws Exception {
        CreateBudgetRequest request = CreateBudgetRequest.builder()
                .categoryId(1L)
                .limitAmount(BigDecimal.valueOf(5000))
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .build();

        BudgetResponse response = BudgetResponse.builder().id(1L).build();

        org.example.app.entity.User user = new org.example.app.entity.User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(java.util.Optional.of(user));
        when(budgetService.createBudget(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void getBudgetReport_shouldReturn200() throws Exception {
        BudgetReportResponse report = BudgetReportResponse.builder()
                .budgetId(1L).categoryName("Food")
                .limitAmount(BigDecimal.valueOf(1000))
                .spentAmount(BigDecimal.valueOf(500))
                .remainingAmount(BigDecimal.valueOf(500))
                .usagePercentage(BigDecimal.valueOf(50))
                .build();

        org.example.app.entity.User user = new org.example.app.entity.User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(java.util.Optional.of(user));
        when(budgetService.getBudgetReport(1L)).thenReturn(report);

        mockMvc.perform(get("/api/budgets/1/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budgetId").value(1))
                .andExpect(jsonPath("$.categoryName").value("Food"))
                .andExpect(jsonPath("$.usagePercentage").value(50));
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void listBudgets_shouldReturn200() throws Exception {
        org.example.app.entity.User user = new org.example.app.entity.User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(java.util.Optional.of(user));

        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isOk());
    }
}
