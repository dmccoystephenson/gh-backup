package com.github.backup;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.kohsuke.github.GHRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class BackupService {

    private final GitHubService gitHubService;
    private final String backupDirectory;

    public BackupService(GitHubService gitHubService, 
                         @Value("${backup.directory}") String backupDirectory) {
        this.gitHubService = gitHubService;
        // Convert to absolute path to support both Linux and Windows
        Path backupPath = Paths.get(backupDirectory).toAbsolutePath().normalize();
        this.backupDirectory = backupPath.toString();
    }

    public void backupUserRepositories(String userOrOrg) throws IOException {
        System.out.println("\nFetching repositories for: " + userOrOrg);
        
        List<GHRepository> repositories = gitHubService.getPublicRepositories(userOrOrg);
        
        if (repositories.isEmpty()) {
            System.out.println("No public repositories found for " + userOrOrg);
            return;
        }

        System.out.println("Found " + repositories.size() + " public repositories");

        Path userBackupPath = Paths.get(backupDirectory, userOrOrg);
        try {
            Files.createDirectories(userBackupPath);
        } catch (IOException e) {
            throw new IOException("Failed to create backup directory '" + userBackupPath + "'", e);
        }

        List<String> failedRepos = new ArrayList<>();
        for (GHRepository repo : repositories) {
            try {
                backupRepository(repo, userBackupPath);
            } catch (Exception e) {
                System.err.println("Failed to backup " + repo.getName() + ": " + e.getMessage());
                failedRepos.add(repo.getName());
            }
        }

        // Display summary of failed repositories
        if (!failedRepos.isEmpty()) {
            System.out.println("\n⚠ Warning: " + failedRepos.size() + " repository(ies) failed to backup:");
            for (String repoName : failedRepos) {
                System.out.println("  - " + repoName);
            }
        }
    }

    public void showBackupStatus() {
        File backupDir = new File(backupDirectory);
        
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            System.out.println("No backups found. Backup directory does not exist: " + backupDirectory);
            return;
        }

        File[] userDirs = backupDir.listFiles(File::isDirectory);
        
        if (userDirs == null || userDirs.length == 0) {
            System.out.println("No backups found.");
            return;
        }

        System.out.println("\nBackup Status");
        System.out.println("=============");
        System.out.println("Backup directory: " + backupDir.getAbsolutePath());
        System.out.println();

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());
        int totalRepos = 0;

        for (File userDir : userDirs) {
            File[] repos = userDir.listFiles(File::isDirectory);
            if (repos == null) {
                System.err.println("Warning: Unable to read directory: " + userDir.getName());
                continue;
            }

            System.out.println(userDir.getName() + "/ (" + repos.length + " repositories)");
            
            for (File repo : repos) {
                totalRepos++;
                long lastModified = repo.lastModified();
                String lastModifiedStr = dateFormat.format(Instant.ofEpochMilli(lastModified));
                System.out.println("  - " + repo.getName() + " (last updated: " + lastModifiedStr + ")");
            }
            System.out.println();
        }

        System.out.println("Total: " + userDirs.length + " users/organizations, " + totalRepos + " repositories");
    }

    private void backupRepository(GHRepository repo, Path backupPath) throws GitAPIException {
        String repoName = repo.getName();
        File localPath = backupPath.resolve(repoName).toFile();

        System.out.print("Backing up " + repoName + "...");

        if (localPath.exists()) {
            // Repository already exists - update it
            try (Git git = Git.open(localPath)) {
                System.out.println(" (repository exists, updating)");
                git.fetch()
                        .setProgressMonitor(new SimpleProgressMonitor())
                        .call();
                System.out.println("  ✓ Updated successfully");
            } catch (IOException e) {
                System.err.println("  ✗ Failed to update: " + e.getMessage());
                throw new GitAPIException("Failed to update repository", e) {};
            }
        } else {
            // Clone new repository
            System.out.println();  // New line for progress output
            try (Git git = Git.cloneRepository()
                    .setURI(repo.getHttpTransportUrl())
                    .setDirectory(localPath)
                    .setProgressMonitor(new SimpleProgressMonitor())
                    .call()) {
                System.out.println("  ✓ Cloned successfully");
            }
        }
    }

    /**
     * Simple progress monitor that shows cloning progress
     */
    private static class SimpleProgressMonitor implements ProgressMonitor {
        private String currentTask;
        private int totalWork;
        private int completed;
        private long lastUpdateTime;
        private static final long UPDATE_INTERVAL_MS = 500; // Update every 500ms

        @Override
        public void start(int totalTasks) {
            // Called when the overall operation starts
        }

        @Override
        public void beginTask(String title, int totalWork) {
            this.currentTask = title;
            this.totalWork = totalWork;
            this.completed = 0;
            this.lastUpdateTime = System.currentTimeMillis();
            if (totalWork > 0) {
                System.out.print("  " + title + ": 0%");
            } else {
                System.out.print("  " + title + "...");
            }
        }

        @Override
        public void update(int completed) {
            this.completed += completed;
            long currentTime = System.currentTimeMillis();
            
            // Throttle updates to avoid too much output
            if (currentTime - lastUpdateTime < UPDATE_INTERVAL_MS) {
                return;
            }
            lastUpdateTime = currentTime;

            if (totalWork > 0 && this.completed < totalWork) {
                int percentage = (int) ((this.completed * 100.0) / totalWork);
                boolean overwriteEnabled = Boolean.parseBoolean(
                        System.getProperty("backup.progress.overwrite", "true"));
                if (overwriteEnabled) {
                    System.out.print("\r  " + currentTask + ": " + percentage + "%");
                } else {
                    System.out.println("  " + currentTask + ": " + percentage + "%");
                }
            }
        }

        @Override
        public void endTask() {
            boolean overwriteEnabled = Boolean.parseBoolean(
                    System.getProperty("backup.progress.overwrite", "true"));
            if (totalWork > 0) {
                if (overwriteEnabled) {
                    System.out.print("\r  " + currentTask + ": 100%");
                } else {
                    System.out.println("  " + currentTask + ": 100%");
                }
            }
            if (overwriteEnabled) {
                System.out.println();
            }
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void showDuration(boolean enabled) {
            // Controls whether the monitor should display operation duration
            // Not implemented in this basic progress monitor
        }
    }
}
