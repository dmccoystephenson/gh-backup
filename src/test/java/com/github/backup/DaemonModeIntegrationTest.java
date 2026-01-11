package com.github.backup;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for daemon mode to verify the entire setup works correctly.
 */
@SpringBootTest
@ActiveProfiles("daemon")
@TestPropertySource(properties = {
    "backup.mode=daemon",
    "backup.scheduled.users=testuser1,testuser2",
    "backup.scheduled.interval.ms=3600000"
})
class DaemonModeIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void daemonModeBeansAreLoaded() {
        // Verify SchedulingConfiguration is loaded
        assertTrue(applicationContext.containsBean("schedulingConfiguration"),
                "SchedulingConfiguration should be loaded in daemon mode");
        
        // Verify ScheduledBackupService is loaded
        assertTrue(applicationContext.containsBean("scheduledBackupService"),
                "ScheduledBackupService should be loaded in daemon mode");
    }

    @Test
    void scheduledBackupServiceIsConfiguredCorrectly() {
        ScheduledBackupService service = applicationContext.getBean(ScheduledBackupService.class);
        assertNotNull(service, "ScheduledBackupService should be available");
        
        // The service should be instantiated without errors
        assertDoesNotThrow(() -> service.runScheduledBackup(),
                "runScheduledBackup should execute without errors");
    }

    @Test
    void backupServiceIsAvailable() {
        // BackupService should be available for ScheduledBackupService to use
        assertTrue(applicationContext.containsBean("backupService"),
                "BackupService should be available in daemon mode");
    }

    @Test
    void gitHubServiceIsAvailable() {
        // GitHubService should be available for BackupService to use
        assertTrue(applicationContext.containsBean("gitHubService"),
                "GitHubService should be available in daemon mode");
    }

    @Test
    void applicationContextLoadsSuccessfully() {
        // The application context should load without errors
        assertNotNull(applicationContext, "Application context should be loaded");
        
        // Verify we're in daemon profile
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        assertEquals(1, activeProfiles.length, "Should have exactly one active profile");
        assertEquals("daemon", activeProfiles[0], "Active profile should be daemon");
    }

    @Test
    void scheduledUsersAreConfigured() {
        // Verify the test properties are being picked up
        String scheduledUsers = applicationContext.getEnvironment().getProperty("backup.scheduled.users");
        assertNotNull(scheduledUsers, "scheduled users property should be set");
        assertTrue(scheduledUsers.contains("testuser1"), "Should contain testuser1");
        assertTrue(scheduledUsers.contains("testuser2"), "Should contain testuser2");
    }

    @Test
    void customIntervalIsConfigured() {
        // Verify custom interval is configured
        String interval = applicationContext.getEnvironment().getProperty("backup.scheduled.interval.ms");
        assertEquals("3600000", interval, "Interval should be set to 1 hour (3600000ms)");
    }

    @Test
    void backupModeIsSetToDaemon() {
        // Verify backup.mode is set to daemon
        String backupMode = applicationContext.getEnvironment().getProperty("backup.mode");
        assertEquals("daemon", backupMode, "backup.mode should be set to daemon");
    }
}
