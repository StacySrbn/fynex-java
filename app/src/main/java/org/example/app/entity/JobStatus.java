package org.example.app.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "export_jobs")
public class ExportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateFrom;
    private LocalDate dateTo;

    @Enumerated(EnumType.STRING)
    private ExportFormat format;

    private String fileUrl;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @ManyToOne
    private User user;
}public enum ExportFormat {
    CSV,
    PDF
}