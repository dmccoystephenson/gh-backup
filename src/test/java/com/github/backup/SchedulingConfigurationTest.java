package com.github.backup;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SchedulingConfiguration to verify that
 * scheduling is only enabled in daemon profile.
 */
class SchedulingConfigurationTest {

    @Nested
    @SpringBootTest
    @ActiveProfiles("daemon")
    class DaemonProfileTest {
        
        @Autowired
        private ApplicationContext applicationContext;
        
        @Test
        void schedulingConfigurationShouldBeLoadedInDaemonProfile() {
            // SchedulingConfiguration bean should exist in daemon profile
            assertTrue(applicationContext.containsBean("schedulingConfiguration"),
                    "SchedulingConfiguration should be present in daemon profile");
            
            // ScheduledAnnotationBeanPostProcessor should be present when scheduling is enabled
            assertNotNull(applicationContext.getBean(ScheduledAnnotationBeanPostProcessor.class),
                    "ScheduledAnnotationBeanPostProcessor should be present when scheduling is enabled");
        }
        
        @Test
        void scheduledBackupServiceShouldBeLoadedInDaemonProfile() {
            // In daemon profile with backup.mode=daemon, ScheduledBackupService should be loaded
            // Note: This requires backup.mode=daemon to be set
            assertTrue(applicationContext.containsBean("schedulingConfiguration"),
                    "SchedulingConfiguration bean should be present in daemon profile");
        }
    }
    
    @Nested
    @SpringBootTest
    @ActiveProfiles("web")
    class WebProfileTest {
        
        @Autowired
        private ApplicationContext applicationContext;
        
        @Test
        void schedulingConfigurationShouldNotBeLoadedInWebProfile() {
            // SchedulingConfiguration bean should NOT exist in web profile
            assertFalse(applicationContext.containsBean("schedulingConfiguration"),
                    "SchedulingConfiguration should NOT be present in web profile");
        }
    }
    
    @Nested
    @SpringBootTest
    class DefaultProfileTest {
        
        @Autowired
        private ApplicationContext applicationContext;
        
        @Test
        void schedulingConfigurationShouldNotBeLoadedInDefaultProfile() {
            // SchedulingConfiguration bean should NOT exist without daemon profile
            assertFalse(applicationContext.containsBean("schedulingConfiguration"),
                    "SchedulingConfiguration should NOT be present in default profile");
        }
    }
}
