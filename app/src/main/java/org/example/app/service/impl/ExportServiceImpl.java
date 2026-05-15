package org.example.app.service.impl;

import org.example.app.dto.*;
import org.example.app.entity.*;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.mapper.ExportMapper;
import org.example.app.repository.ExportJobRepository;
import org.example.app.repository.TransactionRepository;
import org.example.app.repository.UserRepository;
import org.example.app.service.ExportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ExportServiceImpl implements ExportService {

    private final ExportJobRepository exportJobRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ExportMapper exportMapper;

    public ExportServiceImpl(ExportJobRepository exportJobRepository,
                              TransactionRepository transactionRepository,
                              UserRepository userRepository,
                              ExportMapper exportMapper) {
        this.exportJobRepository = exportJobRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.exportMapper = exportMapper;
    }

    @Override
    public ExportResponse startExport(Long userId, ExportRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<Transaction> transactions = transactionRepository
            .findByUserIdAndDateBetween(userId, request.getDateFrom(), request.getDateTo());

        String content;
        if (request.getFormat() == ExportFormat.PDF) {
            content = generatePdfContent(transactions, request);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("id,amount,date,description,type,source,categoryId,categoryName\n");
            for (Transaction t : transactions) {
                sb.append(t.getId()).append(",");
                sb.append(t.getAmount()).append(",");
                sb.append(t.getDate()).append(",");
                sb.append(escapeCsv(t.getDescription())).append(",");
                sb.append(t.getType()).append(",");
                sb.append(t.getSource()).append(",");
                sb.append(t.getCategory() != null ? t.getCategory().getId() : "").append(",");
                sb.append(t.getCategory() != null ? escapeCsv(t.getCategory().getName()) : "").append("\n");
            }
            content = sb.toString();
        }

        ExportJob job = new ExportJob();
        job.setDateFrom(request.getDateFrom());
        job.setDateTo(request.getDateTo());
        job.setFormat(request.getFormat());
        job.setFileUrl(content);
        job.setStatus(JobStatus.DONE);
        job.setUser(user);

        ExportJob saved = exportJobRepository.save(job);
        return exportMapper.toResponse(saved);
    }

    private String generatePdfContent(List<Transaction> transactions, ExportRequest request) {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        StringBuilder sb = new StringBuilder();
        sb.append("================================================================\n");
        sb.append("                    FYNEX FINANCIAL REPORT                       \n");
        sb.append("================================================================\n");
        sb.append("Period: ").append(request.getDateFrom()).append(" to ").append(request.getDateTo()).append("\n");
        sb.append("Generated: ").append(java.time.LocalDateTime.now()).append("\n");
        sb.append("----------------------------------------------------------------\n");
        sb.append(String.format("%-4s %-12s %-12s %-20s %-8s %s\n",
                "ID", "Amount", "Date", "Description", "Type", "Category"));
        sb.append("----------------------------------------------------------------\n");

        for (Transaction t : transactions) {
            sb.append(String.format("%-4d %-12s %-12s %-20s %-8s %s\n",
                    t.getId(),
                    t.getAmount(),
                    t.getDate(),
                    t.getDescription() != null && t.getDescription().length() > 18
                        ? t.getDescription().substring(0, 17) + "~"
                        : (t.getDescription() != null ? t.getDescription() : ""),
                    t.getType(),
                    t.getCategory() != null ? t.getCategory().getName() : ""));
            if (t.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
            } else {
                totalExpense = totalExpense.add(t.getAmount());
            }
        }

        sb.append("----------------------------------------------------------------\n");
        sb.append("SUMMARY:\n");
        sb.append("  Total Income:  ").append(totalIncome).append("\n");
        sb.append("  Total Expense: ").append(totalExpense).append("\n");
        sb.append("  Balance:       ").append(totalIncome.subtract(totalExpense)).append("\n");
        sb.append("================================================================\n");

        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
