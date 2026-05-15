package org.example.app.controller;

import org.example.app.dto.AdminStatsResponse;
import org.example.app.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void getSystemStats_shouldReturn200() throws Exception {
        AdminStatsResponse stats = AdminStatsResponse.builder()
                .totalUsers(10).activeUsers(8).premiumUsers(3).totalTransactions(100)
                .build();

        when(adminService.getSystemStats()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.activeUsers").value(8))
                .andExpect(jsonPath("$.premiumUsers").value(3))
                .andExpect(jsonPath("$.totalTransactions").value(100));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void getAllUsers_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void blockUser_shouldReturn204() throws Exception {
        mockMvc.perform(patch("/api/admin/users/1/block"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void unblockUser_shouldReturn204() throws Exception {
        mockMvc.perform(patch("/api/admin/users/1/unblock"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void changeUserRole_shouldReturn204() throws Exception {
        mockMvc.perform(patch("/api/admin/users/1/role")
                        .param("role", "PREMIUM"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void deleteUser_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void getStats_whenNoData_shouldReturnZeros() throws Exception {
        AdminStatsResponse emptyStats = AdminStatsResponse.builder()
                .totalUsers(0).activeUsers(0).premiumUsers(0).totalTransactions(0)
                .build();

        when(adminService.getSystemStats()).thenReturn(emptyStats);

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(0))
                .andExpect(jsonPath("$.activeUsers").value(0));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    void adminEndpoints_shouldReturn403ForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isForbidden());
    }
}
