package com.github.backup;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration to enable Spring scheduling only in daemon mode.
 * This prevents scheduling from being activated in CLI and web modes.
 */
@Configuration
@EnableScheduling
@Profile("daemon")
public class SchedulingConfiguration {
}
