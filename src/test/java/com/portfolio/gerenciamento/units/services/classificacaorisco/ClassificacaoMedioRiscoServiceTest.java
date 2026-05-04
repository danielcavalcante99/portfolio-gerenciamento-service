package com.portfolio.gerenciamento.units.services.classificacaorisco;

import com.portfolio.gerenciamento.configs.properties.ProjetoProperties;
import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.ClassificacaoRisco;
import com.portfolio.gerenciamento.services.classificacaorisco.ClassificacaoMedioRiscoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ClassificacaoMedioRiscoServiceTest {

    private static final int LIMITE_MAXIMO_MEMBROS = 10;
    private static final int LIMITE_MESES_MEDIO_RISCO = 3;
    private static final BigDecimal LIMITE_ORCAMENTO_MEDIO_RISCO = BigDecimal.valueOf(100_000);
    private static final BigDecimal LIMITE_ORCAMENTO_ALTO_RISCO = BigDecimal.valueOf(500_000);
    private static final LocalDate DATA_INICIO = LocalDate.of(2026, 1, 1);

    private final ClassificacaoMedioRiscoService service = new ClassificacaoMedioRiscoService(defaultProperties());

    @DisplayName("Deve atender regra de medio risco quando orcamento ou duracao ultrapassar limite")
    @ParameterizedTest(name = "orcamento={0}, meses={1}")
    @CsvSource({
            "100000.01, 1",
            "50000, 4"
    })
    void should_returnTrue_when_budgetOrDurationExceedsMediumRiskLimit(BigDecimal orcamento, int meses) {
        Projeto projeto = projeto(orcamento, meses);

        boolean atendeRegra = service.atende(projeto);

        assertThat(atendeRegra).isTrue();
    }

    @Test
    @DisplayName("Deve nao atender regra de medio risco quando orcamento e duracao estiverem no limite")
    void should_returnFalse_when_budgetAndDurationAreWithinMediumRiskLimit() {
        Projeto projeto = projeto(LIMITE_ORCAMENTO_MEDIO_RISCO, LIMITE_MESES_MEDIO_RISCO);

        boolean atendeRegra = service.atende(projeto);

        assertThat(atendeRegra).isFalse();
    }

    @Test
    @DisplayName("Deve retornar classificacao medio quando consultar classificacao da regra")
    void should_returnMediumRiskClassification_when_classificationIsRequested() {
        ClassificacaoRisco classificacao = service.classificacao();

        assertThat(classificacao).isEqualTo(ClassificacaoRisco.MEDIO);
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
