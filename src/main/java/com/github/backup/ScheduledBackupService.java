package com.github.backup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Service that runs scheduled backups at configured intervals.
 * Enabled when backup.mode=daemon.
 */
@Service
@ConditionalOnProperty(name = "backup.mode", havingValue = "daemon")
public class ScheduledBackupService {

    private final BackupService backupService;
    private final List<String> scheduledUsers;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ScheduledBackupService(BackupService backupService,
                                   @Value("${backup.scheduled.users:}") String scheduledUsersConfig) {
        this.backupService = backupService;
        // Parse comma-separated list of users/orgs
        this.scheduledUsers = scheduledUsersConfig.isBlank() 
            ? List.of() 
            : Arrays.stream(scheduledUsersConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @PostConstruct
    public void init() {
        if (scheduledUsers.isEmpty()) {
            System.out.println("⚠ Warning: No users/organizations configured for scheduled backups.");
            System.out.println("Set the 'backup.scheduled.users' property with a comma-separated list.");
            System.out.println("Example: backup.scheduled.users=octocat,github");
        } else {
            System.out.println("GitHub Backup Daemon Mode");
            System.out.println("========================");
            System.out.println("Scheduled backups enabled for: " + String.join(", ", scheduledUsers));
            System.out.println("Backup interval: Every 24 hours");
            System.out.println("First backup will run immediately, then every 24 hours.");
            System.out.println();
        }
    }

    /**
     * Runs backup for all configured users/organizations.
     * Executes on startup and then every 24 hours (86400000 milliseconds).
     */
    @Scheduled(fixedRate = 86400000, initialDelay = 0)
    public void runScheduledBackup() {
        if (scheduledUsers.isEmpty()) {
            return;
        }

        String timestamp = LocalDateTime.now().format(dateTimeFormatter);
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Starting scheduled backup at " + timestamp);
        System.out.println("=".repeat(80));

        for (String userOrOrg : scheduledUsers) {
            try {
                backupService.backupUserRepositories(userOrOrg);
            } catch (IOException e) {
                System.err.println("Error backing up " + userOrOrg + ": " + e.getMessage());
            }
        }

        timestamp = LocalDateTime.now().format(dateTimeFormatter);
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Scheduled backup completed at " + timestamp);
        System.out.println("Next backup will run in 24 hours.");
        System.out.println("=".repeat(80) + "\n");
    }
}
