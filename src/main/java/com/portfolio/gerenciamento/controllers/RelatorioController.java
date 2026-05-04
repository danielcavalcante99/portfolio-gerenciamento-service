package com.portfolio.gerenciamento.controllers;

import com.portfolio.gerenciamento.dtos.response.ErrorResponse;
import com.portfolio.gerenciamento.dtos.response.ResumoPorfolioResponse;
import com.portfolio.gerenciamento.services.report.PortfolioReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/relatorios")
@Tag(
        name = "Relatórios",
        description = "API de relatórios do portfólio com indicadores consolidados de ciclo de vida, orçamento, duração e alocação."
)
@SecurityRequirement(name = "basicAuth")
@RequiredArgsConstructor
public class RelatorioController {

    private final PortfolioReportService portfolioReportService;

    @GetMapping("/resumo-portfolio")
    @Operation(
            summary = "Obter resumo do portfólio",
            description = "Retorna a quantidade de projetos e orçamento total por status, duração média dos projetos encerrados e total de membros únicos alocados."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Resumo gerado.",
            content = @Content(schema = @Schema(implementation = ResumoPorfolioResponse.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Autenticação obrigatória.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Erro inesperado ao gerar relatório.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResumoPorfolioResponse obterResumoPortfolio() {
        return portfolioReportService.getResumo();
    }
}