package com.portfolio.gerenciamento.dtos.request;

import com.portfolio.gerenciamento.enums.Atribuicao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Body da requisição para criação de um membro na API externa simulada de membros.")
public record CriarMembroRequest(
        @Schema(description = "Nome completo do membro.", example = "Ana Silva", maxLength = 150, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 150) String nome,

        @Schema(description = "Atribuição do membro. Apenas com a atribuicao de 'funcionário' pode ser alocado em projetos.", example = "funcionário", maxLength = 80, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Atribuicao atribuicao
) {
}



