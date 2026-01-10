package com.github.backup;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class BackupCommandLineRunner implements CommandLineRunner {

    private final BackupService backupService;

    public BackupCommandLineRunner(BackupService backupService) {
        this.backupService = backupService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java -jar gh-backup.jar <user/org1> [user/org2] ...");
            System.out.println("\nExample: java -jar gh-backup.jar octocat github");
            System.out.println("\nOptional: Set GITHUB_TOKEN environment variable for authenticated access");
            return;
        }

        System.out.println("GitHub Backup Tool");
        System.out.println("==================");
        System.out.println("Backing up repositories for: " + Arrays.toString(args));
        System.out.println();

        for (String userOrOrg : args) {
            try {
                backupService.backupUserRepositories(userOrOrg.trim());
            } catch (Exception e) {
                System.err.println("Error backing up " + userOrOrg + ": " + e.getMessage());
            }
        }

        System.out.println("\nBackup completed!");
    }
}
