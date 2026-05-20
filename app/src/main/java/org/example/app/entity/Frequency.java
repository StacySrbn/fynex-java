package org.example.app.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public enum Frequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}