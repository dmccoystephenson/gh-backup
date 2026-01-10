package com.github.backup;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitHubService {

    private final GitHub github;

    public GitHubService() throws IOException {
        String token = System.getenv("GITHUB_TOKEN");
        GitHub githubClient;
        
        if (token != null && !token.isEmpty()) {
            githubClient = new GitHubBuilder().withOAuthToken(token).build();
            try {
                // Validate that the provided token is valid and has access
                githubClient.getMyself();
                System.out.println("Connected to GitHub with authentication");
            } catch (IOException e) {
                System.err.println("Warning: Invalid or expired GITHUB_TOKEN detected. Falling back to anonymous GitHub access.");
                githubClient = GitHub.connectAnonymously();
                System.out.println("Connected to GitHub anonymously (rate limits apply)");
            }
        } else {
            githubClient = GitHub.connectAnonymously();
            System.out.println("Connected to GitHub anonymously (rate limits apply)");
        }
        this.github = githubClient;
    }

    public List<GHRepository> getPublicRepositories(String userOrOrg) throws IOException {
        IOException orgException = null;
        
        try {
            // Try as organization first
            return github.getOrganization(userOrOrg).listRepositories().toList()
                    .stream()
                    .filter(repo -> !repo.isPrivate())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            orgException = e;
        }
        
        try {
            // If not an organization, try as user
            return github.getUser(userOrOrg).listRepositories().toList()
                    .stream()
                    .filter(repo -> !repo.isPrivate())
                    .collect(Collectors.toList());
        } catch (IOException userException) {
            // Check if this is a 404 (not found) error
            String errorMessage = userException.getMessage();
            if (errorMessage != null && errorMessage.contains("404")) {
                // Actually not found - provide helpful message
                throw new IOException(
                    "'" + userOrOrg + "' not found as GitHub organization or user. " +
                    "Please verify the name is correct.", 
                    userException
                );
            } else {
                // Some other error (rate limit, network, etc.) - preserve original error
                throw userException;
            }
        }
    }
}
