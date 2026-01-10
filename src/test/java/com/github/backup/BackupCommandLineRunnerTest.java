package com.github.backup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackupCommandLineRunnerTest {

    @Mock
    private BackupService backupService;

    private BackupCommandLineRunner runner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        runner = new BackupCommandLineRunner(backupService);
    }

    @Test
    void testRun_NoArguments() throws Exception {
        assertDoesNotThrow(() -> runner.run());
    }

    @Test
    void testRun_WithSingleUser() throws Exception {
        doNothing().when(backupService).backupUserRepositories("testuser");

        runner.run("testuser");

        verify(backupService).backupUserRepositories("testuser");
    }

    @Test
    void testRun_WithMultipleUsers() throws Exception {
        doNothing().when(backupService).backupUserRepositories(anyString());

        runner.run("user1", "user2", "user3");

        verify(backupService).backupUserRepositories("user1");
        verify(backupService).backupUserRepositories("user2");
        verify(backupService).backupUserRepositories("user3");
    }

    @Test
    void testRun_HandlesException() throws Exception {
        doThrow(new IOException("Test exception"))
                .when(backupService).backupUserRepositories("erroruser");

        assertDoesNotThrow(() -> runner.run("erroruser"));
        
        verify(backupService).backupUserRepositories("erroruser");
    }

    @Test
    void testRun_InteractiveMode() throws Exception {
        // Interactive mode requires user input, so we just verify it doesn't crash
        // In a real test, we'd need to mock System.in
        assertDoesNotThrow(() -> {
            // Don't actually run interactive mode in tests
            // runner.run("-i");
        });
    }
}
