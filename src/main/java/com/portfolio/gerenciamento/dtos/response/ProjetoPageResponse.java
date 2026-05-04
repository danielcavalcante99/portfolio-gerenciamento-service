package com.portfolio.gerenciamento.dtos.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resposta paginada para listagem de projetos.")
public record ProjetoPageResponse(
        @ArraySchema(schema = @Schema(implementation = ProjetoResponse.class), arraySchema = @Schema(description = "Projetos na página atual."))
        List<ProjetoResponse> conteudo,

        @Schema(description = "Número da página atual baseado em zero.", example = "0")
        int pagina,

        @Schema(description = "Tamanho da página solicitada.", example = "10")
        int tamanho,

        @Schema(description = "Total de projetos após os filtros.", example = "42")
        long totalElementos,

        @Schema(description = "Total de páginas após os filtros.", example = "5")
        int totalPaginas
) {
}