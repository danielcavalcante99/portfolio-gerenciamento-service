package com.portfolio.gerenciamento.integrations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.gerenciamento.repositories.MembroRepository;
import com.portfolio.gerenciamento.repositories.ProjetoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProjetoRepository projetoRepository;

    @Autowired
    MembroRepository membroRepository;

    @Autowired
    Environment environment;

    @Autowired
    JdbcTemplate jdbcTemplate;

    final ObjectMapper objectMapper = new ObjectMapper();

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("portfolio_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @BeforeEach
    void cleanDatabase() {
        projetoRepository.deleteAll();
        membroRepository.deleteAll();
    }

    protected RequestPostProcessor basicAuth() {
        String username = environment.getRequiredProperty("spring.security.user.name");
        String password = environment.getRequiredProperty("spring.security.user.password");

        return httpBasic(username, password);
    }
}
