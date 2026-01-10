package com.github.backup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduledBackupServiceTest {

    @Mock
    private BackupService backupService;

    private ScheduledBackupService scheduledBackupService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testScheduledBackupService_WithNoUsers() {
        scheduledBackupService = new ScheduledBackupService(backupService, "");
        
        // Should not throw exception with empty users list
        assertDoesNotThrow(() -> scheduledBackupService.runScheduledBackup());
        
        // Should not call backup service when no users configured
        verifyNoInteractions(backupService);
    }

    @Test
    void testScheduledBackupService_WithSingleUser() throws IOException {
        scheduledBackupService = new ScheduledBackupService(backupService, "octocat");
        
        scheduledBackupService.runScheduledBackup();
        
        // Should call backup service once for the configured user
        verify(backupService, times(1)).backupUserRepositories("octocat");
    }

    @Test
    void testScheduledBackupService_WithMultipleUsers() throws IOException {
        scheduledBackupService = new ScheduledBackupService(backupService, "octocat,github,spring-projects");
        
        scheduledBackupService.runScheduledBackup();
        
        // Should call backup service for each configured user
        verify(backupService, times(1)).backupUserRepositories("octocat");
        verify(backupService, times(1)).backupUserRepositories("github");
        verify(backupService, times(1)).backupUserRepositories("spring-projects");
    }

    @Test
    void testScheduledBackupService_WithWhitespace() throws IOException {
        scheduledBackupService = new ScheduledBackupService(backupService, " octocat , github , spring-projects ");
        
        scheduledBackupService.runScheduledBackup();
        
        // Should trim whitespace from user names
        verify(backupService, times(1)).backupUserRepositories("octocat");
        verify(backupService, times(1)).backupUserRepositories("github");
        verify(backupService, times(1)).backupUserRepositories("spring-projects");
    }

    @Test
    void testScheduledBackupService_HandlesException() throws IOException {
        scheduledBackupService = new ScheduledBackupService(backupService, "octocat,github");
        
        doThrow(new IOException("Test exception")).when(backupService).backupUserRepositories("octocat");
        
        // Should continue to next user even if one fails
        assertDoesNotThrow(() -> scheduledBackupService.runScheduledBackup());
        
        verify(backupService, times(1)).backupUserRepositories("octocat");
        verify(backupService, times(1)).backupUserRepositories("github");
    }

    @Test
    void testScheduledBackupService_WithEmptyStrings() throws IOException {
        scheduledBackupService = new ScheduledBackupService(backupService, "octocat,,github");
        
        scheduledBackupService.runScheduledBackup();
        
        // Should filter out empty strings
        verify(backupService, times(1)).backupUserRepositories("octocat");
        verify(backupService, times(1)).backupUserRepositories("github");
        verify(backupService, times(2)).backupUserRepositories(anyString());
    }

    @Test
    void testScheduledBackupService_WithBlankString() {
        scheduledBackupService = new ScheduledBackupService(backupService, "   ");
        
        assertDoesNotThrow(() -> scheduledBackupService.runScheduledBackup());
        
        // Should not call backup service with blank configuration
        verifyNoInteractions(backupService);
    }
}
