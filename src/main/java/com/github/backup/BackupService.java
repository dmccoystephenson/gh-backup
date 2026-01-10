package com.github.backup;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BackupService {

    private final GitHubService gitHubService;
    private final String backupDirectory;

    public BackupService(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
        this.backupDirectory = System.getProperty("backup.directory", "backups");
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
        Files.createDirectories(userBackupPath);

        for (GHRepository repo : repositories) {
            try {
                backupRepository(repo, userBackupPath);
            } catch (Exception e) {
                System.err.println("Failed to backup " + repo.getName() + ": " + e.getMessage());
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
            if (repos == null) continue;

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
            // Update existing repository
            try (Git git = Git.open(localPath)) {
                git.fetch().call();
                System.out.println(" updated");
            } catch (IOException e) {
                System.err.println(" failed to update: " + e.getMessage());
            }
        } else {
            // Clone new repository
            Git.cloneRepository()
                    .setURI(repo.getHttpTransportUrl())
                    .setDirectory(localPath)
                    .call()
                    .close();
            System.out.println(" cloned");
        }
    }
}
