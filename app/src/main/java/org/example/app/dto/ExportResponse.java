package org.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.app.entity.ExportFormat;
import org.example.app.entity.JobStatus;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportResponse {
    private Long id;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private ExportFormat format;
    private String fileUrl;
    private JobStatus status;
}
