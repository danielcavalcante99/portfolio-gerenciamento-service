package com.portfolio.gerenciamento.dtos.response;

import com.portfolio.gerenciamento.enums.ClassificacaoRisco;
import com.portfolio.gerenciamento.enums.ProjetoStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "Dados do projeto retornados pela API. A classificação de risco é calculada dinamicamente e não é armazenada como fonte de verdade.")
public record ProjetoResponse(
        @Schema(description = "Identificador do projeto.", example = "10")
        Long id,

        @Schema(description = "Nome do projeto.", example = "Atualização ERP")
        String nome,

        @Schema(description = "Data de início do projeto.", example = "2026-05-01")
        LocalDate dataInicio,

        @Schema(description = "Data prevista de encerramento.", example = "2026-09-01")
        LocalDate dataPrevistaFim,

        @Schema(description = "Data real de encerramento quando conhecida.", example = "2026-09-15", nullable = true)
        LocalDate dataRealFim,

        @Schema(description = "Orçamento total do projeto.", example = "250000.00")
        BigDecimal orcamentoTotal,

        @Schema(description = "Descrição do projeto.", example = "Modernização do módulo financeiro do ERP.")
        String descricao,

        @Schema(description = "Gerente do projeto. O gerente deve ser um membro válido.")
        MembroResponse gerente,

        @Schema(description = "Status atual do ciclo de vida.", example = "EM_ANALISE")
        ProjetoStatus status,

        @Schema(description = "Classificação de risco dinâmica calculada a partir do orçamento e duração.", example = "MEDIO")
        ClassificacaoRisco classificacaoRisco,

        @ArraySchema(schema = @Schema(implementation = MembroResponse.class), arraySchema = @Schema(description = "Membros alocados no projeto."))
        Set<MembroResponse> membros,

        @Schema(description = "Data e hora de criação.", example = "2026-04-29T10:15:30")
        LocalDateTime dataCriacao,

        @Schema(description = "Data e hora da última atualização.", example = "2026-04-29T11:20:00", nullable = true)
        LocalDateTime dataAtualizacao
) {
}
