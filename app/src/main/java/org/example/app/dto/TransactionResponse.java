package org.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.app.entity.TransactionSource;
import org.example.app.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private TransactionType type;
    private TransactionSource source;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
}
