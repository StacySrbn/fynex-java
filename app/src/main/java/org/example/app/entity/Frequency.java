package org.example.app.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private BigDecimal amount;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer repeatCount;

    private boolean active;

    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    @ManyToOne
    private User user;

    @ManyToOne
    private Category category;
}