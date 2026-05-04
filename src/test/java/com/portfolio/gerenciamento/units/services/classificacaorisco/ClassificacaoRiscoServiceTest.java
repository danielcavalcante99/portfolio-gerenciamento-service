package com.portfolio.gerenciamento.units.services.classificacaorisco;

import com.portfolio.gerenciamento.configs.properties.ProjetoProperties;
import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.ClassificacaoRisco;
import com.portfolio.gerenciamento.services.classificacaorisco.ClassificacaoAltoRiscoService;
import com.portfolio.gerenciamento.services.classificacaorisco.ClassificacaoMedioRiscoService;
import com.portfolio.gerenciamento.services.classificacaorisco.ClassificacaoRiscoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClassificacaoRiscoServiceTest {

    private static final int LIMITE_MAXIMO_MEMBROS = 10;
    private static final int LIMITE_MESES_MEDIO_RISCO = 3;
    private static final BigDecimal LIMITE_ORCAMENTO_MEDIO_RISCO = BigDecimal.valueOf(100_000);
    private static final BigDecimal LIMITE_ORCAMENTO_ALTO_RISCO = BigDecimal.valueOf(500_000);
    private static final LocalDate DATA_INICIO = LocalDate.of(2026, 1, 1);

    private final ClassificacaoRiscoService service = new ClassificacaoRiscoService(List.of(
            new ClassificacaoAltoRiscoService(defaultProperties()),
            new ClassificacaoMedioRiscoService(defaultProperties())
    ));

    @DisplayName("Deve classificar risco quando regras forem avaliadas por prioridade")
    @ParameterizedTest(name = "orcamento={0}, meses={1}, risco={2}")
    @CsvSource({
            "800000, 2, ALTO",
            "200000, 2, MEDIO",
            "50000, 3, BAIXO"
    })
    void should_returnExpectedRisk_when_rulesAreEvaluatedByPriority(
            BigDecimal orcamento,
            int meses,
            ClassificacaoRisco riscoEsperado) {
        Projeto projeto = projeto(orcamento, meses);

        ClassificacaoRisco classificacao = service.classificar(projeto);

        assertThat(classificacao).isEqualTo(riscoEsperado);
    }

    private static ProjetoProperties defaultProperties() {
        return new ProjetoProperties(
                LIMITE_MAXIMO_MEMBROS,
                LIMITE_MESES_MEDIO_RISCO,
                LIMITE_ORCAMENTO_MEDIO_RISCO,
                LIMITE_ORCAMENTO_ALTO_RISCO
        );
    }

    private static Projeto projeto(BigDecimal orcamento, int meses) {
        return Projeto.builder()
                .dataInicio(DATA_INICIO)
                .dataPrevistaFim(DATA_INICIO.plusMonths(meses))
                .orcamentoTotal(orcamento)
                .build();
    }
}
