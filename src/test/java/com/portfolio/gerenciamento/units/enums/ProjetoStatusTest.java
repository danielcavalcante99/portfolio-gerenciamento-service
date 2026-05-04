package com.portfolio.gerenciamento.units.enums;

import com.portfolio.gerenciamento.enums.ProjetoStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ProjetoStatusTest {

    @ParameterizedTest(name = "{0} deve avançar para {1}")
    @CsvSource({
            "EM_ANALISE, ANALISE_REALIZADA",
            "ANALISE_REALIZADA, ANALISE_APROVADA",
            "ANALISE_APROVADA, INICIADO",
            "INICIADO, EM_ANDAMENTO",
            "EM_ANDAMENTO, ENCERRADO"
    })
    @DisplayName("Deve retornar proximo status do fluxo ativo")
    void deveRetornarProximoStatusDoFluxoAtivo(ProjetoStatus atual, ProjetoStatus proximo) {
        assertThat(atual.proximo()).isEqualTo(proximo);
    }

    @ParameterizedTest
    @EnumSource(value = ProjetoStatus.class, names = {"ENCERRADO", "CANCELADO"})
    @DisplayName("Deve retornar null para proximo status quando status for final")
    void deveRetornarNullParaProximoStatusQuandoStatusForFinal(ProjetoStatus status) {
        assertThat(status.proximo()).isNull();
    }

    @ParameterizedTest(name = "{0} -> {1} deve ser {2}")
    @CsvSource({
            "EM_ANALISE, ANALISE_REALIZADA, true",
            "EM_ANALISE, CANCELADO, true",
            "EM_ANALISE, INICIADO, false",
            "ENCERRADO, CANCELADO, true",
            "ENCERRADO, EM_ANDAMENTO, false"
    })
    @DisplayName("Deve validar transicoes permitidas e bloqueadas")
    void deveValidarTransicoesPermitidasEBloqueadas(
            ProjetoStatus origem,
            ProjetoStatus destino,
            boolean permitido) {
        assertThat(origem.podeTransicionarPara(destino)).isEqualTo(permitido);
    }
}
