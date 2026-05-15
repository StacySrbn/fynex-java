package org.example.app.repository;

import org.example.app.entity.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {
    List<ExportJob> findByUserIdOrderByCreatedAtDesc(Long userId);
}
