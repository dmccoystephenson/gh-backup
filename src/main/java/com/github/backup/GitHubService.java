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
        if (token != null && !token.isEmpty()) {
            this.github = new GitHubBuilder().withOAuthToken(token).build();
            System.out.println("Connected to GitHub with authentication");
        } else {
            this.github = GitHub.connectAnonymously();
            System.out.println("Connected to GitHub anonymously (rate limits apply)");
        }
    }

    public List<GHRepository> getPublicRepositories(String userOrOrg) throws IOException {
        try {
            // Try as organization first
            return github.getOrganization(userOrOrg).listRepositories().toList()
                    .stream()
                    .filter(repo -> !repo.isPrivate())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            // If not an organization, try as user
            return github.getUser(userOrOrg).listRepositories().toList()
                    .stream()
                    .filter(repo -> !repo.isPrivate())
                    .collect(Collectors.toList());
        }
    }
}
