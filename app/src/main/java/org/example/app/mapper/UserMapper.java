package org.example.app.mapper;

import org.example.app.dto.CreateUserRequest;
import org.example.app.dto.UserResponse;
import org.example.app.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    public User toEntity(CreateUserRequest request) {
        if (request == null) return null;

        User user = new User();
        user.setEmail(request.getEmail());
        return user;
    }
}
