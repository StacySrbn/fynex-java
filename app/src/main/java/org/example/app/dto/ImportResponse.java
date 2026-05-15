package org.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.app.entity.JobStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportResponse {
    private Long importId;
    private String fileName;
    private JobStatus status;
    private int parsedCount;
    private List<TransactionResponse> preview;
    private String errorLog;
}
