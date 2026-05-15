package org.example.app.service.impl;

import org.example.app.dto.*;
import org.example.app.entity.*;
import org.example.app.exception.FileProcessingException;
import org.example.app.exception.InvalidRequestException;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.mapper.ImportMapper;
import org.example.app.repository.ImportJobRepository;
import org.example.app.repository.TransactionRepository;
import org.example.app.repository.UserRepository;
import org.example.app.service.ImportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ImportServiceImpl implements ImportService {

    private final ImportJobRepository importJobRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ImportMapper importMapper;

    public ImportServiceImpl(ImportJobRepository importJobRepository,
                              TransactionRepository transactionRepository,
                              UserRepository userRepository,
                              ImportMapper importMapper) {
        this.importJobRepository = importJobRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.importMapper = importMapper;
    }

    @Override
    public ImportResponse uploadCsv(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new InvalidRequestException("File must be a CSV");
        }

        String content;
        try {
            content = new String(file.getBytes());
        } catch (IOException e) {
            throw new FileProcessingException("Failed to read file");
        }

        String[] lines = content.split("\n");
        StringBuilder dataBuilder = new StringBuilder();
        List<Transaction> preview = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            dataBuilder.append(line).append("\n");
            String[] parts = line.split(",");
            try {
                Transaction t = new Transaction();
                t.setAmount(new BigDecimal(parts[0].trim()));
                t.setDate(LocalDate.parse(parts[1].trim()));
                if (parts.length > 2) t.setDescription(parts[2].trim());
                if (parts.length > 3) t.setType(TransactionType.valueOf(parts[3].trim().toUpperCase()));
                t.setUser(user);
                preview.add(t);
            } catch (Exception ignored) {
            }
        }

        ImportJob job = new ImportJob();
        job.setFileName(file.getOriginalFilename());
        job.setStatus(JobStatus.PENDING);
        job.setErrorLog(dataBuilder.toString());
        job.setCreatedAt(LocalDateTime.now());
        job.setUser(user);

        ImportJob saved = importJobRepository.save(job);
        return importMapper.toResponse(saved, preview);
    }

    @Override
    public ImportResponse confirmImport(Long importId) {
        ImportJob job = importJobRepository.findById(importId)
            .orElseThrow(() -> new ResourceNotFoundException("ImportJob", importId));

        if (job.getStatus() != JobStatus.PENDING) {
            throw new InvalidRequestException("Import job is not in PENDING status");
        }

        String data = job.getErrorLog();
        if (data == null || data.isEmpty()) {
            throw new InvalidRequestException("No data to import");
        }

        String[] lines = data.split("\n");
        List<Transaction> transactions = new ArrayList<>();

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(",");
            try {
                Transaction t = new Transaction();
                t.setAmount(new BigDecimal(parts[0].trim()));
                t.setDate(LocalDate.parse(parts[1].trim()));
                if (parts.length > 2) t.setDescription(parts[2].trim());
                if (parts.length > 3) t.setType(TransactionType.valueOf(parts[3].trim().toUpperCase()));
                t.setSource(TransactionSource.IMPORT);
                t.setUser(job.getUser());
                transactionRepository.save(t);
                transactions.add(t);
            } catch (Exception ignored) {
            }
        }

        job.setStatus(JobStatus.DONE);
        importJobRepository.save(job);

        return importMapper.toResponse(job, transactions);
    }
}
