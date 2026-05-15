package org.example.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.app.dto.CreateSavingsGoalRequest;
import org.example.app.dto.SavingsGoalResponse;
import org.example.app.service.SavingsGoalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SavingsGoalController.class)
class SavingsGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SavingsGoalService savingsGoalService;

    @MockBean
    private org.example.app.repository.UserRepository userRepository;

    @Test
    @WithMockUser(username = "test@test.com", roles = "PREMIUM")
    void createGoal_shouldReturn201() throws Exception {
        CreateSavingsGoalRequest request = CreateSavingsGoalRequest.builder()
                .name("New Car")
                .targetAmount(BigDecimal.valueOf(20000))
                .icon("car")
                .build();

        SavingsGoalResponse response = SavingsGoalResponse.builder()
                .id(1L).name("New Car").targetAmount(BigDecimal.valueOf(20000))
                .currentAmount(BigDecimal.ZERO).build();

        org.example.app.entity.User user = new org.example.app.entity.User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(java.util.Optional.of(user));
        when(savingsGoalService.createGoal(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/savings-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Car"))
                .andExpect(jsonPath("$.currentAmount").value(0));
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "PREMIUM")
    void listGoals_shouldReturn200() throws Exception {
        org.example.app.entity.User user = new org.example.app.entity.User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(java.util.Optional.of(user));

        mockMvc.perform(get("/api/savings-goals"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "PREMIUM")
    void getGoalById_shouldReturn200() throws Exception {
        SavingsGoalResponse response = SavingsGoalResponse.builder().id(1L).name("Vacation").build();

        org.example.app.entity.User user = new org.example.app.entity.User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(java.util.Optional.of(user));
        when(savingsGoalService.getGoalById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/savings-goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Vacation"));
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "PREMIUM")
    void deleteGoal_shouldReturn204() throws Exception {
        org.example.app.entity.User user = new org.example.app.entity.User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(java.util.Optional.of(user));

        mockMvc.perform(delete("/api/savings-goals/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void premiumEndpoints_shouldReturn403ForUser() throws Exception {
        mockMvc.perform(post("/api/savings-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
