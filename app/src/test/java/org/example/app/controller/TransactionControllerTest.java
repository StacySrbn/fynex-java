package org.example.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.app.dto.*;
import org.example.app.entity.TransactionType;
import org.example.app.service.TransactionService;
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

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private org.example.app.repository.UserRepository userRepository;

    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void createTransaction_shouldReturn201() throws Exception {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .date(LocalDate.of(2024, 6, 15))
                .description("Test")
                .type(TransactionType.INCOME)
                .categoryId(1L)
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id(1L).amount(BigDecimal.valueOf(100)).build();

        org.example.app.entity.User user = new org.example.app.entity.User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(java.util.Optional.of(user));
        when(transactionService.createTransaction(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void getTransactionById_shouldReturn200() throws Exception {
        TransactionResponse response = TransactionResponse.builder().id(1L).build();

        org.example.app.entity.User user = new org.example.app.entity.User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(java.util.Optional.of(user));
        when(transactionService.getTransactionById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void deleteTransaction_shouldReturn204() throws Exception {
        org.example.app.entity.User user = new org.example.app.entity.User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(java.util.Optional.of(user));

        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void listTransactions_shouldReturn200() throws Exception {
        TransactionPageResponse pageResponse = new TransactionPageResponse();
        pageResponse.setPage(0);
        pageResponse.setSize(20);
        pageResponse.setTotalElements(0);
        pageResponse.setTotalPages(0);
        pageResponse.setTransactions(java.util.List.of());

        org.example.app.entity.User user = new org.example.app.entity.User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(java.util.Optional.of(user));
        when(transactionService.getUserTransactions(anyLong(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void listTransactions_withFilters_shouldCallService() throws Exception {
        TransactionPageResponse pageResponse = new TransactionPageResponse();
        pageResponse.setPage(0);
        pageResponse.setSize(10);
        pageResponse.setTotalElements(1);
        pageResponse.setTotalPages(1);
        pageResponse.setTransactions(java.util.List.of());

        org.example.app.entity.User user = new org.example.app.entity.User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(java.util.Optional.of(user));
        when(transactionService.getUserTransactions(anyLong(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/transactions")
                        .param("type", "EXPENSE")
                        .param("categoryId", "5")
                        .param("dateFrom", "2024-01-01")
                        .param("dateTo", "2024-12-31")
                        .param("search", "test")
                        .param("sortBy", "amount")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}
