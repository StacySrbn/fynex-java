package org.example.app.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

public enum JobStatus {
    PENDING,
    PROCESSING,
    DONE,
    FAILED
}