package org.example.app.service;

import org.example.app.dto.*;
import org.example.app.entity.Role;

import java.util.List;

public interface AdminService {

    AdminStatsResponse getSystemStats();

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    void blockUser(Long userId);

    void unblockUser(Long userId);

    void changeUserRole(Long userId, Role newRole);
}
