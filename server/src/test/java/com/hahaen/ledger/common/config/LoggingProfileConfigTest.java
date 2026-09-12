package com.hahaen.ledger.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggingProfileConfigTest {

    private final YamlPropertySourceLoader yamlLoader = new YamlPropertySourceLoader();

    @Test
    void shouldUseConfiguredLogDirectoryForEachProfile() throws Exception {
        assertEquals("D:/github/log/haji", yamlLoader
                .load("dev", new ClassPathResource("application-dev.yml"))
                .getFirst()
                .getProperty("logging.file.path"));
        assertEquals("/home/hahaen/log/haji", yamlLoader
                .load("prod", new ClassPathResource("application-prod.yml"))
                .getFirst()
                .getProperty("logging.file.path"));
    }
}
