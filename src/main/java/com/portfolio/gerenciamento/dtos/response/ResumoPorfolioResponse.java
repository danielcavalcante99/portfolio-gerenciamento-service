package com.portfolio.gerenciamento.dtos.response;

import com.portfolio.gerenciamento.enums.ProjetoStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "Relatório resumo do portfólio com contagem de projetos, orçamentos, média de duração e total de membros alocados.")
public record ResumoPorfolioResponse(
        @Schema(description = "Quantidade de projetos agrupados por status do ciclo de vida.", example = "{\"EM_ANALISE\":2,\"EM_ANDAMENTO\":1}")
        Map<ProjetoStatus, Long> contagemProjetosPorStatus,

        @Schema(description = "Orçamento total agrupado por status do ciclo de vida.", example = "{\"EM_ANALISE\":350000.00,\"EM_ANDAMENTO\":900000.00}")
        Map<ProjetoStatus, BigDecimal> orcamentoTotalPorStatus,

        @Schema(description = "Duração média em dias dos projetos com status ENCERRADO e data real de encerramento informada.", example = "126.5")
        Double mediaDuracaoProjetosEncerradosEmDias,

        @Schema(description = "Total de membros distintos alocados em pelo menos um projeto.", example = "14")
        Long totalMembrosUnicosAlocados
) {
}
