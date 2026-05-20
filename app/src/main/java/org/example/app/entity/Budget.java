package org.example.app.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal limitAmount;

    private LocalDate startDate;

    private LocalDate endDate;

    @ManyToOne
    private User user;

    @ManyToOne
    private Category category;
}