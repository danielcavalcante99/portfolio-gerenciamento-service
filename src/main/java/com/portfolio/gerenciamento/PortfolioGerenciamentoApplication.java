package com.portfolio.gerenciamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PortfolioGerenciamentoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortfolioGerenciamentoApplication.class, args);
    }

}
