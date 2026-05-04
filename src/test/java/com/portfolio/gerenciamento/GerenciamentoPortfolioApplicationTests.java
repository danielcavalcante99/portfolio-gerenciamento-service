package com.portfolio.gerenciamento;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;

class GerenciamentoPortfolioApplicationTests {

    @Test
    @DisplayName("Deve instanciar aplicacao quando construtor padrao for chamado")
    void applicationMainDeveSerInstanciavel() {
        assertDoesNotThrow(PortfolioGerenciamentoApplication::new);
    }

    @Test
    @DisplayName("Deve delegar inicializacao para SpringApplication")
    void mainDeveDelegarInicializacaoParaSpringApplication() {
        String[] args = {"--spring.profiles.active=test"};

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            PortfolioGerenciamentoApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(PortfolioGerenciamentoApplication.class, args));
        }
    }

}
