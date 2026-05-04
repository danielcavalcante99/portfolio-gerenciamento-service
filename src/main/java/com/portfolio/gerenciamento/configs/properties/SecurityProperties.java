package com.portfolio.gerenciamento.configs.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spring.security.user")
public record SecurityProperties(

        @NotBlank
        String name,

        @NotBlank
        String password

) {
}