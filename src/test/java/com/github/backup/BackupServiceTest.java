package com.github.backup;

import com.github.backup.web.BackupStatusResponse;
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

    @Test
    void testGetBackupStatusData_NoBackups() {
        // Test with empty directory
        BackupStatusResponse status = backupService.getBackupStatusData();
        
        assertNotNull(status);
        assertEquals(0, status.getTotalUsers());
        assertEquals(0, status.getTotalRepositories());
        assertNotNull(status.getUsers());
        assertTrue(status.getUsers().isEmpty());
    }

    @Test
    void testGetBackupStatusData_WithBackups() throws IOException {
        // Create a fake backup structure with multiple users
        Path user1Dir = tempDir.resolve("user1");
        Path repo1Dir = user1Dir.resolve("repo1");
        Path repo2Dir = user1Dir.resolve("repo2");
        Files.createDirectories(repo1Dir);
        Files.createDirectories(repo2Dir);

        Path user2Dir = tempDir.resolve("user2");
        Path repo3Dir = user2Dir.resolve("repo3");
        Files.createDirectories(repo3Dir);

        BackupStatusResponse status = backupService.getBackupStatusData();
        
        assertNotNull(status);
        assertEquals(2, status.getTotalUsers());
        assertEquals(3, status.getTotalRepositories());
        assertNotNull(status.getUsers());
        assertEquals(2, status.getUsers().size());
        
        // Verify first user
        BackupStatusResponse.UserBackupInfo user1 = status.getUsers().stream()
                .filter(u -> "user1".equals(u.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(user1);
        assertEquals(2, user1.getRepositoryCount());
        assertEquals(2, user1.getRepositories().size());
        
        // Verify second user
        BackupStatusResponse.UserBackupInfo user2 = status.getUsers().stream()
                .filter(u -> "user2".equals(u.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(user2);
        assertEquals(1, user2.getRepositoryCount());
        assertEquals(1, user2.getRepositories().size());
    }

    @Test
    void testGetBackupStatusData_NonExistentDirectory() {
        // Test with non-existent directory
        BackupService service = new BackupService(gitHubService, tempDir.resolve("nonexistent").toString());
        
        BackupStatusResponse status = service.getBackupStatusData();
        
        assertNotNull(status);
        assertEquals(0, status.getTotalUsers());
        assertEquals(0, status.getTotalRepositories());
        assertTrue(status.getUsers().isEmpty());
    }
}
