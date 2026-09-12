package com.github.backup;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "usage.reporting.enabled=false") // never report to the trace service from a test
class GhBackupApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures the Spring application context loads successfully
    }
}
