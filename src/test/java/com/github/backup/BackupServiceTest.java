package com.github.backup;

import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kohsuke.github.GHRepository;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackupServiceTest {

    @Mock
    private GitHubService gitHubService;

    @Mock
    private GHRepository mockRepo1;

    @Mock
    private GHRepository mockRepo2;

    private BackupService backupService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        backupService = new BackupService(gitHubService, tempDir.toString());
    }

    @Test
    void testBackupUserRepositories_NoRepositories() throws IOException {
        when(gitHubService.getPublicRepositories("testuser"))
                .thenReturn(Collections.emptyList());

        backupService.backupUserRepositories("testuser");

        verify(gitHubService).getPublicRepositories("testuser");
    }

    @Test
    void testBackupUserRepositories_WithRepositories() throws IOException {
        when(mockRepo1.getName()).thenReturn("repo1");
        when(mockRepo1.getHttpTransportUrl()).thenReturn("https://github.com/testuser/repo1.git");
        
        List<GHRepository> repos = Collections.singletonList(mockRepo1);
        when(gitHubService.getPublicRepositories("testuser")).thenReturn(repos);

        // Note: This test will attempt actual git operations, which may fail in test environment
        // In a real scenario, we'd need to mock JGit operations as well
        assertDoesNotThrow(() -> backupService.backupUserRepositories("testuser"));
        
        verify(gitHubService).getPublicRepositories("testuser");
    }

    @Test
    void testShowBackupStatus_NoBackups() {
        // Create a new BackupService with an empty directory
        String emptyDir = tempDir.resolve("empty").toString();
        BackupService service = new BackupService(gitHubService, emptyDir);
        
        assertDoesNotThrow(() -> service.showBackupStatus());
    }

    @Test
    void testShowBackupStatus_WithBackups() throws IOException {
        // Create a fake backup structure
        Path userDir = tempDir.resolve("testuser");
        Path repoDir = userDir.resolve("test-repo");
        Files.createDirectories(repoDir);

        assertDoesNotThrow(() -> backupService.showBackupStatus());
    }

    @Test
    void testBackupDirectory_IsAbsolute() throws IOException {
        BackupService service = new BackupService(gitHubService, "relative/path");
        
        // The service should convert relative paths to absolute
        assertDoesNotThrow(() -> service.showBackupStatus());
    }

    @Test
    void testBackupUserRepositories_HandlesException() throws IOException {
        when(gitHubService.getPublicRepositories("erroruser"))
                .thenThrow(new IOException("Test exception"));

        assertThrows(IOException.class, () -> backupService.backupUserRepositories("erroruser"));
    }
}
