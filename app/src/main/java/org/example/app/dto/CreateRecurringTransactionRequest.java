package org.example.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.app.entity.Frequency;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRecurringTransactionRequest {
    @NotBlank
    private String title;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    private Integer repeatCount;

    @NotNull
    private Frequency frequency;

    @NotNull
    private Long categoryId;
}
