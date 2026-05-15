package org.example.app.service;

import org.example.app.dto.*;
import org.springframework.web.multipart.MultipartFile;

public interface ImportService {

    ImportResponse uploadCsv(Long userId, MultipartFile file);

    ImportResponse confirmImport(Long importId);
}
