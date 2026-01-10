package com.github.backup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GitHubServiceTest {

    @Test
    void testGitHubServiceInitialization_Anonymous() {
        assertDoesNotThrow(() -> {
            GitHubService service = new GitHubService();
            assertNotNull(service);
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GITHUB_TOKEN", matches = ".+")
    void testGitHubServiceInitialization_WithToken() {
        assertDoesNotThrow(() -> {
            GitHubService service = new GitHubService();
            assertNotNull(service);
        });
    }

    @Test
    void testGetPublicRepositories_InvalidUser() throws IOException {
        GitHubService service = new GitHubService();
        
        // Test with a very unlikely username
        assertThrows(IOException.class, () -> 
            service.getPublicRepositories("this-user-definitely-does-not-exist-12345678")
        );
    }
}
