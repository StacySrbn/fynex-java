package org.example.app.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

public enum UserStatus {
    ACTIVE,
    BLOCKED
}