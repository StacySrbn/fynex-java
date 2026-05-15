package org.example.app.service;

import org.example.app.dto.*;
import org.example.app.entity.*;
import org.example.app.mapper.UserMapper;
import org.example.app.repository.TransactionRepository;
import org.example.app.repository.UserRepository;
import org.example.app.service.impl.UserAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAdminServiceImpl userAdminService;

    @Test
    void createUser_shouldCreateUser() {
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .build();
        User user = new User();
        user.setEmail("test@example.com");
        User saved = new User();
        saved.setId(1L);
        saved.setEmail("test@example.com");
        saved.setRole(Role.USER);
        saved.setStatus(UserStatus.ACTIVE);
        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();

        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-pass");
        when(userRepository.save(user)).thenReturn(saved);
        when(userMapper.toResponse(saved)).thenReturn(response);

        UserResponse result = userAdminService.createUser(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(Role.USER, result.getRole());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        verify(userMapper).toEntity(request);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(user);
        verify(userMapper).toResponse(saved);
    }

    @Test
    void blockUser_shouldSetStatusBlocked() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userAdminService.blockUser(userId);

        assertEquals(UserStatus.BLOCKED, user.getStatus());
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }

    @Test
    void changeUserRole_shouldUpdateRole() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setRole(Role.USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userAdminService.changeUserRole(userId, Role.PREMIUM);

        assertEquals(Role.PREMIUM, user.getRole());
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }

    @Test
    void getSystemStats_shouldReturnCounts() {
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByStatus(UserStatus.ACTIVE)).thenReturn(80L);
        when(userRepository.countByRole(Role.PREMIUM)).thenReturn(25L);
        when(transactionRepository.count()).thenReturn(5000L);

        AdminStatsResponse result = userAdminService.getSystemStats();

        assertNotNull(result);
        assertEquals(100L, result.getTotalUsers());
        assertEquals(80L, result.getActiveUsers());
        assertEquals(25L, result.getPremiumUsers());
        assertEquals(5000L, result.getTotalTransactions());
        assertNotNull(result.getAdditionalStats());
        assertTrue(result.getAdditionalStats().isEmpty());
        verify(userRepository).count();
        verify(userRepository).countByStatus(UserStatus.ACTIVE);
        verify(userRepository).countByRole(Role.PREMIUM);
        verify(transactionRepository).count();
    }

    @Test
    void unblockUser_shouldSetStatusActive() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.BLOCKED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userAdminService.unblockUser(userId);

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }
}
