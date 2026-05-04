package com.portfolio.gerenciamento.units.services.report;

import com.portfolio.gerenciamento.dtos.response.ResumoPorfolioResponse;
import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.ProjetoStatus;
import com.portfolio.gerenciamento.repositories.ProjetoRepository;
import com.portfolio.gerenciamento.services.report.PortfolioReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioReportServiceTest {

    private static final LocalDate DATA_INICIO = LocalDate.of(2026, 1, 1);
    private static final LocalDate DATA_REAL_FIM_DEZ_DIAS_DEPOIS = LocalDate.of(2026, 1, 11);
    private static final BigDecimal ORCAMENTO_PROJETO_ENCERRADO = BigDecimal.valueOf(100);
    private static final BigDecimal ORCAMENTO_OUTRO_PROJETO_ENCERRADO = BigDecimal.valueOf(200);
    private static final BigDecimal ORCAMENTO_PROJETO_EM_ANALISE = BigDecimal.valueOf(50);
    private static final BigDecimal ORCAMENTO_TOTAL_ENCERRADO = BigDecimal.valueOf(300);
    private static final long TOTAL_MEMBROS_ALOCADOS = 7L;
    private static final double DURACAO_MEDIA_DEZ_DIAS = 10.0;

    @Mock
    private ProjetoRepository projetoRepository;

    @InjectMocks
    private PortfolioReportService service;

    @Test
    @DisplayName("Deve gerar resumo consolidado quando houver projetos em diferentes status")
    void should_returnConsolidatedSummary_when_projectsHaveDifferentStatuses() {
        Projeto encerradoComData = projeto(
                ProjetoStatus.ENCERRADO,
                ORCAMENTO_PROJETO_ENCERRADO,
                DATA_REAL_FIM_DEZ_DIAS_DEPOIS
        );
        Projeto encerradoSemData = projeto(ProjetoStatus.ENCERRADO, ORCAMENTO_OUTRO_PROJETO_ENCERRADO, null);
        Projeto emAnalise = projeto(ProjetoStatus.EM_ANALISE, ORCAMENTO_PROJETO_EM_ANALISE, null);

        when(projetoRepository.findAll()).thenReturn(List.of(encerradoComData, encerradoSemData, emAnalise));
        when(projetoRepository.countDistinctMembrosAlocados()).thenReturn(TOTAL_MEMBROS_ALOCADOS);

        ResumoPorfolioResponse response = service.getResumo();

        assertThat(response.contagemProjetosPorStatus())
                .containsEntry(ProjetoStatus.ENCERRADO, 2L)
                .containsEntry(ProjetoStatus.EM_ANALISE, 1L);
        assertThat(response.orcamentoTotalPorStatus().get(ProjetoStatus.ENCERRADO))
                .isEqualByComparingTo(ORCAMENTO_TOTAL_ENCERRADO);
        assertThat(response.orcamentoTotalPorStatus().get(ProjetoStatus.EM_ANALISE))
                .isEqualByComparingTo(ORCAMENTO_PROJETO_EM_ANALISE);
        assertThat(response.mediaDuracaoProjetosEncerradosEmDias()).isEqualTo(DURACAO_MEDIA_DEZ_DIAS);
        assertThat(response.totalMembrosUnicosAlocados()).isEqualTo(TOTAL_MEMBROS_ALOCADOS);
    }

    @Test
    @DisplayName("Deve retornar duracao media zero quando nao houver projeto encerrado com data real")
    void should_returnZeroAverageDuration_when_thereIsNoClosedProjectWithActualEndDate() {
        Projeto projetoAtivo = projeto(ProjetoStatus.EM_ANDAMENTO, BigDecimal.TEN, null);

        when(projetoRepository.findAll()).thenReturn(List.of(projetoAtivo));
        when(projetoRepository.countDistinctMembrosAlocados()).thenReturn(0L);

        ResumoPorfolioResponse response = service.getResumo();

        assertThat(response.mediaDuracaoProjetosEncerradosEmDias()).isZero();
    }

    private static Projeto projeto(ProjetoStatus status, BigDecimal orcamento, LocalDate dataRealFim) {
        return Projeto.builder()
                .status(status)
                .orcamentoTotal(orcamento)
                .dataInicio(DATA_INICIO)
                .dataRealFim(dataRealFim)
                .build();
    }
}
