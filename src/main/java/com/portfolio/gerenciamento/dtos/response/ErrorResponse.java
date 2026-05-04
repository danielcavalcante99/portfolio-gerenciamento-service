package com.portfolio.gerenciamento.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Resposta de erro padrão retornada pela API.")
public record ErrorResponse(
        @Schema(description = "Momento em que o erro ocorreu.", example = "2026-04-29T10:15:30")
        LocalDateTime timestamp,

        @Schema(description = "Código de status HTTP.", example = "400")
        int status,

        @Schema(description = "Motivo do erro HTTP.", example = "Bad Request")
        String erro,

        @Schema(description = "Mensagem de erro de negócio, validação ou infraestrutura.", example = "Transição de status inválida de EM_ANALISE para INICIADO")
        String mensagem,

        @Schema(description = "Caminho da requisição que gerou o erro.", example = "/api/projetos/1/status")
        String caminho,

        @Schema(description = "Erros de validação por campo. Vazio para erros que não são de validação.", example = "{\"nome\":\"não deve estar em branco\"}")
        Map<String, String> campoErros
) {
}
