package com.portfolio.gerenciamento.controllers;

import com.portfolio.gerenciamento.dtos.request.CriarProjetoRequest;
import com.portfolio.gerenciamento.dtos.request.ProjetoFiltroRequest;
import com.portfolio.gerenciamento.dtos.request.AtualizarStatusProjetoRequest;
import com.portfolio.gerenciamento.dtos.request.AtualizarProjetoRequest;
import com.portfolio.gerenciamento.dtos.response.ErrorResponse;
import com.portfolio.gerenciamento.dtos.response.ProjetoPageResponse;
import com.portfolio.gerenciamento.dtos.response.ProjetoResponse;
import com.portfolio.gerenciamento.services.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projetos")
@RequiredArgsConstructor
@Tag(
    name = "Projetos",
    description = "API para gestão do ciclo de vida, atualização, exclusão e alocação de membros em projetos."
)
@SecurityRequirement(name = "basicAuth")
public class ProjetoController {

    private final ProjetoService projetoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar projeto",
            description = "Cria um projeto com gerente válido, status atual, orçamento e datas planejadas. As datas são validadas e a classificação de risco é calculada dinamicamente."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        description = "Dados para criação do projeto. O gerenteId deve referenciar um membro existente.",
        content = @Content(
            schema = @Schema(implementation = CriarProjetoRequest.class),
            examples = @ExampleObject(value = """
                {
                  "nome": "Atualização ERP",
                  "dataInicio": "2026-05-01",
                  "dataFimPrevista": "2026-09-01",
                  "dataFimReal": null,
                  "orcamentoTotal": 250000.00,
                  "descricao": "Modernização do módulo financeiro do ERP.",
                  "gerenteId": 1,
                  "status": "EM_ANALISE"
                }
                """)
        )
    )
    @ApiResponse(
            responseCode = "201",
            description = "Projeto criado.",
            content = @Content(schema = @Schema(implementation = ProjetoResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Payload inválido, datas inválidas ou erro de validação.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(
            responseCode = "401",
            description = "Autenticação obrigatória.",
            content = @Content)
    @ApiResponse(
            responseCode = "404",
            description = "Gerente não encontrado.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ProjetoResponse criar(@Valid @RequestBody CriarProjetoRequest request) {
        return projetoService.criar(request);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar projeto por id",
            description = "Retorna um projeto com gerente, membros alocados e classificação de risco calculada dinamicamente."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Projeto encontrado.",
            content = @Content(schema = @Schema(implementation = ProjetoResponse.class)))
    @ApiResponse(
            responseCode = "401",
            description = "Autenticação obrigatória.",
            content = @Content)
    @ApiResponse(
            responseCode = "404",
            description = "Projeto não encontrado.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ProjetoResponse buscarPeloId(
            @Parameter(description = "Identificador do projeto.", example = "10", required = true)
            @PathVariable Long id
    ) {
        return projetoService.buscarPeloId(id);
    }

    @GetMapping
    @Operation(
            summary = "Listar projetos com paginação e filtros opcionais",
            description = "Retorna uma lista paginada de projetos. Os filtros podem ser combinados por nome, status, gerente, datas e classificação de risco dinâmica."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Projetos retornados.",
            content = @Content(schema = @Schema(implementation = ProjetoPageResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Enum, data ou parâmetro de paginação inválido.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(
            responseCode = "401",
            description = "Autenticação obrigatória.",
            content = @Content)
    public ProjetoPageResponse buscarTodos(
            @ParameterObject ProjetoFiltroRequest filtro,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        return projetoService.buscarTodos(filtro, pageable);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar projeto",
            description = "Atualiza os dados principais do projeto e o gerente, sem alterar o status do ciclo de vida. As regras de datas e orçamento são revalidadas."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        description = "Dados para atualizar o projeto, exceto status e alocações de membros.",
        content = @Content(
            schema = @Schema(implementation = AtualizarProjetoRequest.class),
            examples = @ExampleObject(value = """
                {
                  "nome": "Atualização ERP",
                  "dataInicio": "2026-05-01",
                  "dataFimPrevista": "2026-10-01",
                  "dataFimReal": null,
                  "orcamentoTotal": 300000.00,
                  "descricao": "Plano atualizado do projeto.",
                  "gerenteId": 1
                }
                """)
        )
    )
    @ApiResponse(
            responseCode = "200",
            description = "Projeto atualizado.",
            content = @Content(schema = @Schema(implementation = ProjetoResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Payload inválido, datas inválidas ou erro de validação.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(
            responseCode = "401",
            description = "Autenticação obrigatória.",
            content = @Content)
    @ApiResponse(
            responseCode = "404",
            description = "Projeto ou gerente não encontrado.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ProjetoResponse atualizar(
            @Parameter(description = "Identificador do projeto.", example = "10", required = true)
            @PathVariable Long id,
            @Valid @RequestBody AtualizarProjetoRequest request
    ) {
        return projetoService.atualizar(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Alterar status do projeto",
            description = "Altera o status do ciclo de vida do projeto. O fluxo normal não permite pular etapas; CANCELADO é permitido a partir de qualquer status."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        description = "Status de destino.",
        content = @Content(
            schema = @Schema(implementation = AtualizarStatusProjetoRequest.class),
            examples = @ExampleObject(value = """
                {
                  "status": "ANALISE_REALIZADA"
                }
                """)
        )
    )
    @ApiResponse(
            responseCode = "200",
            description = "Status alterado.",
            content = @Content(schema = @Schema(implementation = ProjetoResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Transição de status inválida ou payload inválido.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(
            responseCode = "401",
            description = "Autenticação obrigatória.",
            content = @Content)
    @ApiResponse(
            responseCode = "404",
            description = "Projeto não encontrado.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ProjetoResponse atualizarStatus(
            @Parameter(description = "Identificador do projeto.", example = "10", required = true)
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusProjetoRequest request
    ) {
        return projetoService.atualizarStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Excluir projeto quando o status permitir",
            description = "Exclui um projeto somente quando ele não estiver com status INICIADO, EM_ANDAMENTO ou ENCERRADO."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Projeto excluído.", content = @Content)
    @ApiResponse(
            responseCode = "400",
            description = "Status do projeto não permite exclusão.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(
            responseCode = "401",
            description = "Autenticação obrigatória.",
            content = @Content)
    @ApiResponse(
            responseCode = "404",
            description = "Projeto não encontrado.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public void deletar(
            @Parameter(description = "Identificador do projeto.", example = "10", required = true)
            @PathVariable Long id
    ) {
        projetoService.deletar(id);
    }

    @PostMapping("/{projetoId}/membros/{membroId}")
    @Operation(
            summary = "Associar membro ao projeto",
            description = "Associa um membro existente com atribuição 'funcionário'. Bloqueia associação duplicada, projetos acima do limite de membros e membros já alocados no limite de projetos ativos."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Membro associado.",
            content = @Content(schema = @Schema(implementation = ProjetoResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Atribuição inválida, associação duplicada ou limite de alocação atingido.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(
            responseCode = "401",
            description = "Autenticação obrigatória.",
            content = @Content)
    @ApiResponse(responseCode = "404",
            description = "Projeto ou membro não encontrado.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ProjetoResponse adicionarMembro(
            @Parameter(description = "Identificador do projeto.", example = "10", required = true)
            @PathVariable Long projetoId,
            @Parameter(description = "Identificador do membro a ser alocado. A atribuição deve ser 'funcionário'.", example = "2", required = true)
            @PathVariable Long membroId
    ) {
        return projetoService.adicionarMembro(projetoId, membroId);
    }

    @DeleteMapping("/{projetoId}/membros/{membroId}")
    @Operation(
            summary = "Remover membro do projeto",
            description = "Remove um membro do projeto preservando o mínimo de 1 membro alocado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Membro removido.",
            content = @Content(schema = @Schema(implementation = ProjetoResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Membro não está alocado ao projeto ou a remoção violaria a regra mínima de alocação.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(
            responseCode = "401",
            description = "Autenticação obrigatória.",
            content = @Content)
    @ApiResponse(
            responseCode = "404",
            description = "Projeto ou membro não encontrado.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ProjetoResponse removerMembro(
            @Parameter(description = "Identificador do projeto.", example = "10", required = true)
            @PathVariable Long projetoId,
            @Parameter(description = "Identificador do membro a ser removido.", example = "2", required = true)
            @PathVariable Long membroId
    ) {
        return projetoService.removerMembro(projetoId, membroId);
    }
}