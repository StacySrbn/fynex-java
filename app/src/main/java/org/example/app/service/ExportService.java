package org.example.app.service;

import org.example.app.dto.*;

public interface ExportService {

    ExportResponse startExport(Long userId, ExportRequest request);
}
