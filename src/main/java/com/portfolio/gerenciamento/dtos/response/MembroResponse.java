package com.portfolio.gerenciamento.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Dados do membro retornados pela API externa simulada de membros.")
public record MembroResponse(
        @Schema(description = "Id do membro.", example = "1")
        Long id,

        @Schema(description = "Nome completo do membro.", example = "Ana Silva")
        String nome,

        @Schema(description = "Atribuição do membro. Apenas 'funcionário' pode ser alocado em projetos.", example = "funcionário")
        String atribuicao,

        @Schema(description = "Data e hora de criação.", example = "2026-04-29T10:15:30")
        LocalDateTime dataCriacao,

        @Schema(description = "Data e hora da última atualização.", example = "2026-04-29T11:20:00", nullable = true)
        LocalDateTime dataAtualizacao
) {
}