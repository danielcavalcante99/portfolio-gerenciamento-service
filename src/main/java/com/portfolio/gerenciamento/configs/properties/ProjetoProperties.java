package com.portfolio.gerenciamento.configs.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Validated
@ConfigurationProperties(prefix = "portfolio.projeto")
public record ProjetoProperties(

        @Max(10)
        int maxMembros,

        @Min(1)
        int maxAlocacoesAtivas,

        @NotNull
        BigDecimal limiteOrcamentoBaixo,

        @NotNull
        BigDecimal limiteOrcamentoAlto
) {
}