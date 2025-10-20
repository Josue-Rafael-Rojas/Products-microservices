package com.inventory.inventory.config;

import okhttp3.mockwebserver.MockWebServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public MockWebServer mockWebServer() {
        return new MockWebServer();
    }
}