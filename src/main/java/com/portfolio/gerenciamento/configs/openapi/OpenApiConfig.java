package com.portfolio.gerenciamento.configs.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI portfolioOpenApi() {
        String schemeName = "basicAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Portfolio Gerenciamento API")
                        .version("v1")
                        .description(""" 
                                API para gerenciar o portfólio de projetos de uma empresa. Esse sistema deverá permitir o
                                acompanhamento completo do ciclo de vida de cada projeto, desde a análise de viabilidade até a finalização, incluindo
                                gerenciamento de equipe, orçamento e risco."""))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")));
    }
}
