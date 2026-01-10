package com.github.backup.web;

import com.github.backup.BackupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Collections;

/**
 * REST controller for backup management operations.
 * 
 * Note on CSRF: CSRF protection is not required for this API because:
 * 1. The API is designed to be consumed by the same-origin frontend
 * 2. Spring Boot disables CSRF by default for stateless REST APIs
 * 3. No session state is maintained between requests
 */
@RestController
@RequestMapping("/api/backups")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping
    public ResponseEntity<BackupResponse> createBackup(@Valid @RequestBody BackupRequest request) {
        try {
            backupService.backupUserRepositories(request.getUserOrOrg());
            return ResponseEntity.ok(new BackupResponse(true, "Backup completed successfully for " + request.getUserOrOrg()));
        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .body(new BackupResponse(false, "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<BackupStatusResponse> getStatus() {
        try {
            BackupStatusResponse status = backupService.getBackupStatusData();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new BackupStatusResponse(0, 0, Collections.emptyList()));
        }
    }
}
