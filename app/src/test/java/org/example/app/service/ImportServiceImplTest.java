package org.example.app.service;

import org.example.app.dto.ImportResponse;
import org.example.app.entity.*;
import org.example.app.exception.FileProcessingException;
import org.example.app.exception.InvalidRequestException;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.mapper.ImportMapper;
import org.example.app.repository.ImportJobRepository;
import org.example.app.repository.TransactionRepository;
import org.example.app.repository.UserRepository;
import org.example.app.service.impl.ImportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportServiceImplTest {

    @Mock
    private ImportJobRepository importJobRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImportMapper importMapper;

    @InjectMocks
    private ImportServiceImpl importService;

    @Test
    void uploadCsv_withValidFile_shouldParseAndReturnPreview() {
        Long userId = 1L;
        String csvContent = "amount,date,description,type\n100,2024-06-15,Groceries,EXPENSE\n200,2024-06-16,Salary,INCOME\n";
        MultipartFile file = new MockMultipartFile("file", "transactions.csv", "text/csv", csvContent.getBytes());

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        ImportJob savedJob = new ImportJob();
        savedJob.setId(1L);
        when(importJobRepository.save(any(ImportJob.class))).thenReturn(savedJob);
        when(importMapper.toResponse(any(ImportJob.class), anyList())).thenReturn(
                ImportResponse.builder().importId(1L).parsedCount(2).build()
        );

        ImportResponse result = importService.uploadCsv(userId, file);

        assertNotNull(result);
        assertEquals(1L, result.getImportId());
        verify(userRepository).findById(userId);
        verify(importJobRepository).save(any(ImportJob.class));
        verify(importMapper).toResponse(any(ImportJob.class), anyList());
    }

    @Test
    void uploadCsv_withInvalidExtension_shouldThrow() {
        Long userId = 1L;
        MultipartFile file = new MockMultipartFile("file", "data.txt", "text/plain", "data".getBytes());

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        assertThrows(InvalidRequestException.class, () -> importService.uploadCsv(userId, file));
        verify(importJobRepository, never()).save(any());
    }

    @Test
    void uploadCsv_whenUserNotFound_shouldThrow() {
        Long userId = 999L;
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "a,b\n1,2".getBytes());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> importService.uploadCsv(userId, file));
        verify(importJobRepository, never()).save(any());
    }

    @Test
    void uploadCsv_withEmptyFile_shouldReturnZeroParsed() {
        Long userId = 1L;
        String csvContent = "amount,date\n";
        MultipartFile file = new MockMultipartFile("file", "empty.csv", "text/csv", csvContent.getBytes());

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        ImportJob savedJob = new ImportJob();
        savedJob.setId(2L);
        when(importJobRepository.save(any(ImportJob.class))).thenReturn(savedJob);
        when(importMapper.toResponse(any(ImportJob.class), anyList())).thenReturn(
                ImportResponse.builder().importId(2L).parsedCount(0).build()
        );

        ImportResponse result = importService.uploadCsv(userId, file);

        assertNotNull(result);
        assertEquals(2L, result.getImportId());
        assertEquals(0, result.getParsedCount());
    }

    @Test
    void confirmImport_withValidJob_shouldSaveTransactions() {
        Long importId = 1L;
        ImportJob job = new ImportJob();
        job.setId(importId);
        job.setStatus(JobStatus.PENDING);
        job.setErrorLog("100,2024-06-15,Groceries,EXPENSE\n200,2024-06-16,Salary,INCOME\n");
        User user = new User();
        user.setId(1L);
        job.setUser(user);

        when(importJobRepository.findById(importId)).thenReturn(Optional.of(job));
        when(importMapper.toResponse(any(ImportJob.class), anyList())).thenReturn(
                ImportResponse.builder().importId(importId).parsedCount(2).status(JobStatus.DONE).build()
        );

        ImportResponse result = importService.confirmImport(importId);

        assertNotNull(result);
        assertEquals(importId, result.getImportId());
        assertEquals(JobStatus.DONE, result.getStatus());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(importJobRepository).save(job);
    }

    @Test
    void confirmImport_whenJobNotFound_shouldThrow() {
        Long importId = 999L;
        when(importJobRepository.findById(importId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> importService.confirmImport(importId));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void confirmImport_whenJobNotPending_shouldThrow() {
        Long importId = 1L;
        ImportJob job = new ImportJob();
        job.setId(importId);
        job.setStatus(JobStatus.DONE);

        when(importJobRepository.findById(importId)).thenReturn(Optional.of(job));

        assertThrows(InvalidRequestException.class, () -> importService.confirmImport(importId));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void confirmImport_withEmptyData_shouldThrow() {
        Long importId = 1L;
        ImportJob job = new ImportJob();
        job.setId(importId);
        job.setStatus(JobStatus.PENDING);
        job.setErrorLog("");

        when(importJobRepository.findById(importId)).thenReturn(Optional.of(job));

        assertThrows(InvalidRequestException.class, () -> importService.confirmImport(importId));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void confirmImport_withInvalidLines_shouldSkipBadLines() {
        Long importId = 1L;
        ImportJob job = new ImportJob();
        job.setId(importId);
        job.setStatus(JobStatus.PENDING);
        job.setErrorLog("100,2024-06-15,Groceries,EXPENSE\ninvalid,line\n200,2024-06-16,Salary,INCOME\n");
        User user = new User();
        user.setId(1L);
        job.setUser(user);

        when(importJobRepository.findById(importId)).thenReturn(Optional.of(job));
        when(importMapper.toResponse(any(ImportJob.class), anyList())).thenReturn(
                ImportResponse.builder().importId(importId).parsedCount(2).status(JobStatus.DONE).build()
        );

        ImportResponse result = importService.confirmImport(importId);

        assertNotNull(result);
        assertEquals(JobStatus.DONE, result.getStatus());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }
}
