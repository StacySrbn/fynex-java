package org.example.app.mapper;

import org.example.app.dto.ExportResponse;
import org.example.app.entity.ExportJob;
import org.springframework.stereotype.Component;

@Component
public class ExportMapper {

    public ExportResponse toResponse(ExportJob job) {
        if (job == null) return null;

        ExportResponse response = new ExportResponse();
        response.setId(job.getId());
        response.setDateFrom(job.getDateFrom());
        response.setDateTo(job.getDateTo());
        response.setFormat(job.getFormat());
        response.setFileUrl(job.getFileUrl());
        response.setStatus(job.getStatus());
        return response;
    }
}
