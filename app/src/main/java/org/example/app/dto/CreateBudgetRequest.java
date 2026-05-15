package org.example.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBudgetRequest {
    @NotNull
    private Long categoryId;

    @NotNull
    @Positive
    private BigDecimal limitAmount;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
