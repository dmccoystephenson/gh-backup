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

    private void backupRepository(GHRepository repo, Path backupPath) throws GitAPIException {
        String repoName = repo.getName();
        File localPath = backupPath.resolve(repoName).toFile();

        System.out.print("Backing up " + repoName + "...");

        if (localPath.exists()) {
            // Update existing repository
            try (Git git = Git.open(localPath)) {
                git.fetch().call();
                git.pull().call();
                System.out.println(" updated");
            } catch (IOException e) {
                System.err.println(" failed to update: " + e.getMessage());
            }
        } else {
            // Clone new repository
            Git.cloneRepository()
                    .setURI(repo.getHtmlUrl().toString())
                    .setDirectory(localPath)
                    .call()
                    .close();
            System.out.println(" cloned");
        }
    }
}
