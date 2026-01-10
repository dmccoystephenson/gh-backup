package com.github.backup;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Scanner;

@Component
public class BackupCommandLineRunner implements CommandLineRunner {

    private final BackupService backupService;

    public BackupCommandLineRunner(BackupService backupService) {
        this.backupService = backupService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if interactive mode is requested
        if (args.length > 0 && ("-i".equals(args[0]) || "--interactive".equals(args[0]))) {
            runInteractiveMode();
            return;
        }

        if (args.length == 0) {
            System.out.println("Usage: java -jar gh-backup.jar [options] <user/org1> [user/org2] ...");
            System.out.println("\nOptions:");
            System.out.println("  -i, --interactive    Start interactive mode");
            System.out.println("\nExample: java -jar gh-backup.jar octocat github");
            System.out.println("         java -jar gh-backup.jar -i");
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

    private void runInteractiveMode() {
        System.out.println("GitHub Backup Tool - Interactive Mode");
        System.out.println("=====================================");
        System.out.println();
        
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\nCommands:");
            System.out.println("  backup <user/org>  - Backup repositories for a user or organization");
            System.out.println("  status             - View current backup status");
            System.out.println("  exit               - Exit interactive mode");
            System.out.print("\n> ");

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split("\\s+", 2);
            String command = parts[0].toLowerCase();

            switch (command) {
                case "backup":
                    if (parts.length < 2) {
                        System.out.println("Error: Please specify a user or organization");
                        System.out.println("Usage: backup <user/org>");
                    } else {
                        String userOrOrg = parts[1].trim();
                        try {
                            backupService.backupUserRepositories(userOrOrg);
                        } catch (Exception e) {
                            System.err.println("Error backing up " + userOrOrg + ": " + e.getMessage());
                        }
                    }
                    break;

                case "status":
                    backupService.showBackupStatus();
                    break;

                case "exit":
                case "quit":
                    System.out.println("Exiting interactive mode...");
                    running = false;
                    break;

                default:
                    System.out.println("Unknown command: " + command);
                    System.out.println("Type 'exit' to quit or use one of the available commands.");
                    break;
            }
        }

        // Note: We intentionally don't close the scanner since it wraps System.in
        // Closing it would close System.in for the entire JVM process
    }
}
