package com.github.backup.web;

import com.github.backup.BackupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/backups")
@CrossOrigin(origins = "*")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping
    public ResponseEntity<BackupResponse> createBackup(@RequestBody BackupRequest request) {
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
                    .body(new BackupStatusResponse(0, 0, null));
        }
    }
}
