package org.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionPageResponse {
    private List<TransactionResponse> transactions;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
