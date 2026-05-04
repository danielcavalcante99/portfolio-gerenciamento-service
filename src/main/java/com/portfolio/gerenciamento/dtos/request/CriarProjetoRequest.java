package com.portfolio.gerenciamento.dtos.request;

import com.portfolio.gerenciamento.enums.ProjetoStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Body da requisi\u00e7\u00e3o para cria\u00e7\u00e3o de um projeto. O gerente deve referenciar um membro existente com atribui\u00e7\u00e3o GERENTE.")
public record CriarProjetoRequest(
        @Schema(description = "Nome do projeto.", example = "Atualiza\u00e7\u00e3o ERP", maxLength = 150, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 150) String nome,

        @Schema(description = "Data de in\u00edcio do projeto. As datas prevista e real de encerramento n\u00e3o podem ser anteriores a esta data.", example = "2026-05-01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull LocalDate dataInicio,

        @Schema(description = "Data prevista de encerramento. Deve ser igual ou posterior \u00e0 data de in\u00edcio.", example = "2026-09-01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull LocalDate dataPrevistaFim,

        @Schema(description = "Data real de encerramento quando conhecida. Deve ser igual ou posterior \u00e0 data de in\u00edcio.", example = "2026-09-15", nullable = true)
        LocalDate dataRealFim,

        @Schema(description = "Or\u00e7amento total do projeto. Usado com as datas para calcular o risco dinamicamente.", example = "250000.00", minimum = "0.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @DecimalMin("0.00") BigDecimal orcamentoTotal,

        @Schema(description = "Descri\u00e7\u00e3o do projeto.", example = "Moderniza\u00e7\u00e3o do m\u00f3dulo financeiro do ERP.")
        String descricao,

        @Schema(description = "ID do membro existente respons\u00e1vel por gerenciar o projeto. O membro deve possuir atribui\u00e7\u00e3o GERENTE.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Long gerenteId,

        @Schema(description = "Status inicial do projeto. Os valores de status s\u00e3o fixos e as transi\u00e7\u00f5es s\u00e3o validadas pela camada de servi\u00e7o.", example = "EM_ANALISE", allowableValues = {
                "EM_ANALISE", "ANALISE_REALIZADA", "ANALISE_APROVADA", "INICIADO", "EM_ANDAMENTO", "ENCERRADO", "CANCELADO"
        }, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull ProjetoStatus status
) {
}
