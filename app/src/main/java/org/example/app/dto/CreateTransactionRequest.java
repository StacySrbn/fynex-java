package org.example.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.app.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTransactionRequest {
    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private LocalDate date;

    private String description;

    @NotNull
    private TransactionType type;

    @NotNull
    private Long categoryId;
}
