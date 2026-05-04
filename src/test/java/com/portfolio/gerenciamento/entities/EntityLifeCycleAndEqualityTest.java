package com.portfolio.gerenciamento.entities;

import com.portfolio.gerenciamento.enums.Atribuicao;
import com.portfolio.gerenciamento.enums.ProjetoStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class EntityLifeCycleAndEqualityTest {

    @Test
    @DisplayName("Deve preencher datas de criação e atualização em membro")
    void devePreencherDatasDeCriacaoAtualizacaoEmMembro() {
        Membro membro = getMembro();

        membro.prePersist();
        membro.preUpdate();

        assertThat(membro.getDataCriacao()).isNotNull();
        assertThat(membro.getDataAtualizacao()).isNotNull();
    }

    @Test
    @DisplayName("Deve preencher datas de criação e atualização em projeto")
    void devePreencherDatasDeCriacaoAtualizacaoEmProjeto() {
        Projeto projeto = getProjeto();

        projeto.prePersist();
        projeto.preUpdate();

        assertThat(projeto.getDataCriacao()).isNotNull();
        assertThat(projeto.getDataAtualizacao()).isNotNull();
    }

    private static Membro getMembro() {
        return Membro.builder()
                .nome("Ana")
                .atribuicao(Atribuicao.FUNCIONARIO)
                .build();
    }

    private static Projeto getProjeto() {
        return Projeto.builder()
                .id(1L)
                .nome("Projeto")
                .dataInicio(LocalDate.of(2026, 1, 1))
                .dataPrevistaFim(LocalDate.of(2026, 7, 1))
                .orcamentoTotal(BigDecimal.valueOf(100_000))
                .status(ProjetoStatus.EM_ANALISE)
                .build();
    }
}
