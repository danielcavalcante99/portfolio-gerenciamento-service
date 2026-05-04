package com.portfolio.gerenciamento.dtos.request;

import com.portfolio.gerenciamento.enums.ProjetoStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Body da requisição para alteração do status do projeto.")
public record AtualizarStatusProjetoRequest(
        @Schema(description = "Status de destino do projeto. O fluxo normal não pode pular etapas; CANCELADO é permitido a partir de qualquer status atual.",
                example = "ANALISE_REALIZADA",
                allowableValues = {
                        "EM_ANALISE", "ANALISE_REALIZADA", "ANALISE_APROVADA", "INICIADO", "EM_ANDAMENTO", "ENCERRADO", "CANCELADO"
                },
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull ProjetoStatus status
) {
}
