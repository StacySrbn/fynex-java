package org.example.app.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private LocalDate date;

    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionSource source;

    @ManyToOne
    private User user;

    @ManyToOne
    private Category category;
}
public enum TransactionType {
    INCOME,
    EXPENSE
}public enum TransactionSource {
    MANUAL,
    IMPORT,
    RECURRING
}