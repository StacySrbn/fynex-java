package org.example.app.service;

import org.example.app.dto.*;
import org.example.app.entity.*;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.mapper.ExportMapper;
import org.example.app.repository.ExportJobRepository;
import org.example.app.repository.TransactionRepository;
import org.example.app.repository.UserRepository;
import org.example.app.service.impl.ExportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceImplTest {

    @Mock
    private ExportJobRepository exportJobRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExportMapper exportMapper;

    @InjectMocks
    private ExportServiceImpl exportService;

    @Test
    void startExport_withCsvFormat_shouldGenerateCsv() {
        Long userId = 1L;
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        ExportRequest request = ExportRequest.builder()
                .dateFrom(from)
                .dateTo(to)
                .format(ExportFormat.CSV)
                .build();

        User user = new User();
        user.setId(userId);

        Category cat = new Category();
        cat.setId(10L);
        cat.setName("Food");

        Transaction t1 = new Transaction();
        t1.setId(1L);
        t1.setAmount(BigDecimal.valueOf(100));
        t1.setDate(LocalDate.of(2024, 6, 15));
        t1.setDescription("Groceries");
        t1.setType(TransactionType.EXPENSE);
        t1.setSource(TransactionSource.MANUAL);
        t1.setCategory(cat);

        List<Transaction> transactions = Arrays.asList(t1);
        ExportJob savedJob = new ExportJob();
        savedJob.setId(1L);
        ExportResponse response = ExportResponse.builder().id(1L).format(ExportFormat.CSV).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserIdAndDateBetween(userId, from, to)).thenReturn(transactions);

        ArgumentCaptor<ExportJob> captor = ArgumentCaptor.forClass(ExportJob.class);
        when(exportJobRepository.save(captor.capture())).thenReturn(savedJob);
        when(exportMapper.toResponse(savedJob)).thenReturn(response);

        ExportResponse result = exportService.startExport(userId, request);

        assertNotNull(result);
        assertEquals(ExportFormat.CSV, result.getFormat());

        ExportJob captured = captor.getValue();
        assertEquals(from, captured.getDateFrom());
        assertEquals(to, captured.getDateTo());
        assertEquals(ExportFormat.CSV, captured.getFormat());
        assertEquals(JobStatus.DONE, captured.getStatus());
        assertTrue(captured.getFileUrl().contains("id,amount,date,description"));
        assertTrue(captured.getFileUrl().contains("100"));
        assertTrue(captured.getFileUrl().contains("Groceries"));

        verify(userRepository).findById(userId);
        verify(transactionRepository).findByUserIdAndDateBetween(userId, from, to);
        verify(exportJobRepository).save(any(ExportJob.class));
    }

    @Test
    void startExport_withPdfFormat_shouldGenerateFormattedReport() {
        Long userId = 1L;
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        ExportRequest request = ExportRequest.builder()
                .dateFrom(from)
                .dateTo(to)
                .format(ExportFormat.PDF)
                .build();

        User user = new User();
        user.setId(userId);

        Transaction income = new Transaction();
        income.setId(1L);
        income.setAmount(BigDecimal.valueOf(5000));
        income.setDate(LocalDate.of(2024, 6, 1));
        income.setType(TransactionType.INCOME);
        income.setSource(TransactionSource.MANUAL);

        Transaction expense = new Transaction();
        expense.setId(2L);
        expense.setAmount(BigDecimal.valueOf(2000));
        expense.setDate(LocalDate.of(2024, 6, 15));
        expense.setType(TransactionType.EXPENSE);
        expense.setSource(TransactionSource.MANUAL);

        List<Transaction> transactions = Arrays.asList(income, expense);
        ExportJob savedJob = new ExportJob();
        savedJob.setId(1L);
        ExportResponse response = ExportResponse.builder().id(1L).format(ExportFormat.PDF).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserIdAndDateBetween(userId, from, to)).thenReturn(transactions);

        ArgumentCaptor<ExportJob> captor = ArgumentCaptor.forClass(ExportJob.class);
        when(exportJobRepository.save(captor.capture())).thenReturn(savedJob);
        when(exportMapper.toResponse(savedJob)).thenReturn(response);

        ExportResponse result = exportService.startExport(userId, request);

        assertNotNull(result);

        ExportJob captured = captor.getValue();
        assertTrue(captured.getFileUrl().contains("FYNEX FINANCIAL REPORT"));
        assertTrue(captured.getFileUrl().contains("Total Income"));
        assertTrue(captured.getFileUrl().contains("5000"));
        assertTrue(captured.getFileUrl().contains("2000"));
        assertTrue(captured.getFileUrl().contains("Balance"));

        verify(userRepository).findById(userId);
        verify(transactionRepository).findByUserIdAndDateBetween(userId, from, to);
        verify(exportJobRepository).save(any(ExportJob.class));
    }

    @Test
    void startExport_withNoTransactions_shouldGenerateEmptyReport() {
        Long userId = 1L;
        ExportRequest request = ExportRequest.builder()
                .dateFrom(LocalDate.of(2024, 1, 1))
                .dateTo(LocalDate.of(2024, 12, 31))
                .format(ExportFormat.CSV)
                .build();

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserIdAndDateBetween(userId, request.getDateFrom(), request.getDateTo()))
                .thenReturn(List.of());

        ExportJob savedJob = new ExportJob();
        savedJob.setId(1L);
        when(exportJobRepository.save(any())).thenReturn(savedJob);
        when(exportMapper.toResponse(savedJob)).thenReturn(ExportResponse.builder().id(1L).build());

        ExportResponse result = exportService.startExport(userId, request);

        assertNotNull(result);
        verify(transactionRepository).findByUserIdAndDateBetween(userId, request.getDateFrom(), request.getDateTo());
    }

    @Test
    void startExport_shouldThrowWhenUserNotFound() {
        Long userId = 999L;
        ExportRequest request = ExportRequest.builder()
                .dateFrom(LocalDate.of(2024, 1, 1))
                .dateTo(LocalDate.of(2024, 12, 31))
                .format(ExportFormat.CSV)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> exportService.startExport(userId, request));
        verify(transactionRepository, never()).findByUserIdAndDateBetween(any(), any(), any());
    }

    @Test
    void startExport_withCsvSpecialChars_shouldEscapeProperly() {
        Long userId = 1L;
        ExportRequest request = ExportRequest.builder()
                .dateFrom(LocalDate.of(2024, 1, 1))
                .dateTo(LocalDate.of(2024, 12, 31))
                .format(ExportFormat.CSV)
                .build();

        User user = new User();
        user.setId(userId);

        Transaction t = new Transaction();
        t.setId(1L);
        t.setAmount(BigDecimal.valueOf(50));
        t.setDate(LocalDate.of(2024, 6, 15));
        t.setDescription("Food, drinks & \"snacks\"");
        t.setType(TransactionType.EXPENSE);
        t.setSource(TransactionSource.MANUAL);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserIdAndDateBetween(userId, request.getDateFrom(), request.getDateTo()))
                .thenReturn(Arrays.asList(t));
        when(exportJobRepository.save(any())).thenReturn(new ExportJob());
        when(exportMapper.toResponse(any())).thenReturn(ExportResponse.builder().id(1L).build());

        exportService.startExport(userId, request);

        ArgumentCaptor<ExportJob> captor = ArgumentCaptor.forClass(ExportJob.class);
        verify(exportJobRepository).save(captor.capture());
        assertTrue(captor.getValue().getFileUrl().contains("\"Food, drinks & \"\"snacks\"\"\""));
    }
}
