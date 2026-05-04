package com.portfolio.gerenciamento.dtos.request;

import com.portfolio.gerenciamento.enums.ClassificacaoRisco;
import com.portfolio.gerenciamento.enums.ProjetoStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Schema(description = "Filtros opcionais para consulta paginada de projetos.")
public record ProjetoFiltroRequest(

        @Schema(description = "Filtro parcial pelo nome do projeto.", example = "Sistema de Pagamentos")
        String nome,

        @Schema(description = "Status atual do projeto.", example = "EM_ANDAMENTO")
        ProjetoStatus status,

        @Schema(description = "Identificador do gerente responsável pelo projeto.", example = "10")
        Long gerenteId,

        @Schema(description = "Filtra projetos pela data de início.", example = "2026-01-15")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataInicio,

        @Schema(description = "Filtra projetos pela data prevista de encerramento.", example = "2026-12-31")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataFimPrevista,

        @Schema(description = "Classificação de risco calculada dinamicamente.", example = "ALTO")
        ClassificacaoRisco classificacaoRisco

) {
}