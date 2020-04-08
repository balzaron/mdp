package com.miotech.mdp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import javax.sql.DataSource;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

@org.springframework.boot.test.context.TestConfiguration
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
public class TestConfiguration {
    private volatile PostgreSQLContainer PostgreS;

    @Value("${testsuite.postgresImage}")
    private String postgresImageName;

    @Bean
    public DataSource testingDataSource() {
        if (PostgreS == null) {
            initializeDataSource();
        }
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(PostgreS.getJdbcUrl());
        hikariConfig.setUsername(PostgreS.getUsername());
        hikariConfig.setPassword(PostgreS.getPassword());
        hikariConfig.setDriverClassName(PostgreS.getDriverClassName());
        return new HikariDataSource(hikariConfig);
    }

    private void initializeDataSource() {
        WaitStrategy waitStrategy = new LogMessageWaitStrategy()
                .withRegEx(".*database system is ready to accept connections.*\\s")
                .withTimes(1)
                .withStartupTimeout(Duration.of(60, SECONDS));

        PostgreS = new PostgreSQLContainer<>(postgresImageName)
                .withUsername("agens")
                .withDatabaseName("agens")
                .withCommand("tail -f /home/agens/AgensGraph/data/logfile")
                .waitingFor(waitStrategy);
        PostgreS.start();
    }
}
