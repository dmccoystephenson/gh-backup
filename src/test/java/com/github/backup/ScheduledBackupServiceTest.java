package com.github.backup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduledBackupServiceTest {

    private static final long DEFAULT_INTERVAL = 86400000L; // 24 hours

    @Mock
    private BackupService backupService;

    private ScheduledBackupService scheduledBackupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testScheduledBackupService_WithNoUsers() {
        scheduledBackupService = new ScheduledBackupService(backupService, "", DEFAULT_INTERVAL);
        
        // Should not throw exception with empty users list
        assertDoesNotThrow(() -> scheduledBackupService.runScheduledBackup());
        
        // Should not call backup service when no users configured
        verifyNoInteractions(backupService);
    }

    @Test
    void testScheduledBackupService_WithSingleUser() throws IOException {
        scheduledBackupService = new ScheduledBackupService(backupService, "octocat", DEFAULT_INTERVAL);
        
        scheduledBackupService.runScheduledBackup();
        
        // Should call backup service once for the configured user
        verify(backupService, times(1)).backupUserRepositories("octocat");
    }

    @Test
    void testScheduledBackupService_WithMultipleUsers() throws IOException {
        scheduledBackupService = new ScheduledBackupService(backupService, "octocat,github,spring-projects", DEFAULT_INTERVAL);
        
        scheduledBackupService.runScheduledBackup();
        
        // Should call backup service for each configured user
        verify(backupService, times(1)).backupUserRepositories("octocat");
        verify(backupService, times(1)).backupUserRepositories("github");
        verify(backupService, times(1)).backupUserRepositories("spring-projects");
    }

    @Test
    void testScheduledBackupService_WithWhitespace() throws IOException {
        scheduledBackupService = new ScheduledBackupService(backupService, " octocat , github , spring-projects ", DEFAULT_INTERVAL);
        
        scheduledBackupService.runScheduledBackup();
        
        // Should trim whitespace from user names
        verify(backupService, times(1)).backupUserRepositories("octocat");
        verify(backupService, times(1)).backupUserRepositories("github");
        verify(backupService, times(1)).backupUserRepositories("spring-projects");
    }

    @Test
    void testScheduledBackupService_HandlesException() throws IOException {
        scheduledBackupService = new ScheduledBackupService(backupService, "octocat,github", DEFAULT_INTERVAL);
        
        doThrow(new IOException("Test exception")).when(backupService).backupUserRepositories("octocat");
        
        // Should continue to next user even if one fails
        assertDoesNotThrow(() -> scheduledBackupService.runScheduledBackup());
        
        verify(backupService, times(1)).backupUserRepositories("octocat");
        verify(backupService, times(1)).backupUserRepositories("github");
    }

    @Test
    void testScheduledBackupService_WithEmptyStrings() throws IOException {
        scheduledBackupService = new ScheduledBackupService(backupService, "octocat,,github", DEFAULT_INTERVAL);
        
        scheduledBackupService.runScheduledBackup();
        
        // Should filter out empty strings and only call for valid users
        verify(backupService, times(1)).backupUserRepositories("octocat");
        verify(backupService, times(1)).backupUserRepositories("github");
        verifyNoMoreInteractions(backupService);
    }

    @Test
    void testScheduledBackupService_WithBlankString() {
        scheduledBackupService = new ScheduledBackupService(backupService, "   ", DEFAULT_INTERVAL);
        
        assertDoesNotThrow(() -> scheduledBackupService.runScheduledBackup());
        
        // Should not call backup service with blank configuration
        verifyNoInteractions(backupService);
    }

    @Test
    void testScheduledBackupService_WithCustomInterval() {
        long customInterval = 3600000L; // 1 hour
        scheduledBackupService = new ScheduledBackupService(backupService, "octocat", customInterval);
        
        // Should not throw exception with custom interval
        assertDoesNotThrow(() -> scheduledBackupService.runScheduledBackup());
    }
}
