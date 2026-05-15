package org.example.app.mapper;

import org.example.app.dto.ImportResponse;
import org.example.app.dto.TransactionResponse;
import org.example.app.entity.ImportJob;
import org.example.app.entity.Transaction;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ImportMapper {

    private final TransactionMapper transactionMapper;

    public ImportMapper(TransactionMapper transactionMapper) {
        this.transactionMapper = transactionMapper;
    }

    public ImportResponse toResponse(ImportJob job, List<Transaction> transactions) {
        if (job == null) return null;

        ImportResponse response = new ImportResponse();
        response.setImportId(job.getId());
        response.setFileName(job.getFileName());
        response.setStatus(job.getStatus());
        response.setErrorLog(job.getErrorLog());

        List<TransactionResponse> preview = transactions != null
            ? transactions.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList())
            : Collections.emptyList();
        response.setPreview(preview);
        response.setParsedCount(preview.size());

        return response;
    }
}
