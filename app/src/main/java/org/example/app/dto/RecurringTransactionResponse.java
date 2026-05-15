package org.example.app.dto;

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
public class RecurringTransactionResponse {
    private Long id;
    private String title;
    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer repeatCount;
    private boolean active;
    private Frequency frequency;
    private Long userId;
    private Long categoryId;
    private String categoryName;
}
