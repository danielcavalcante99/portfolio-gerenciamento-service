package com.portfolio.gerenciamento.controllers;


import com.portfolio.gerenciamento.dtos.request.CriarMembroRequest;
import com.portfolio.gerenciamento.dtos.response.ErrorResponse;
import com.portfolio.gerenciamento.dtos.response.MembroResponse;
import com.portfolio.gerenciamento.services.MembroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/membros")
@Tag(
        name = "Membros",
        description = "API externa simulada de membros utilizada para criar e consultar membros antes de serem referenciados por projetos."
)
@SecurityRequirement(name = "basicAuth")
@RequiredArgsConstructor
public class MembroController {

    private final MembroService membroService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar um membro",
            description = "Cria um membro na API externa simulada. A alocação em projetos posteriormente exige que o cargo seja exatamente 'funcionário'."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Nome e cargo do membro.",
            content = @Content(
                    schema = @Schema(implementation = CriarMembroRequest.class),
                    examples = @ExampleObject(value = """
                            {
                              "nome": "Ana Silva",
                              "atribuicao": "FUNCIONARIO"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "201",
            description = "Membro criado.",
            content = @Content(schema = @Schema(implementation = MembroResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Corpo da requisição inválido ou erro de validação.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Autenticação obrigatória.",
            content = @Content
    )
    public MembroResponse criar(@Valid @RequestBody CriarMembroRequest request) {
        return membroService.criar(request);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar membro por id",
            description = "Retorna um membro válido que pode ser utilizado como gerente de projeto ou, quando o cargo for 'funcionário', ser alocado em projetos."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Membro encontrado.",
            content = @Content(schema = @Schema(implementation = MembroResponse.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Autenticação obrigatória.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "Membro não encontrado.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public MembroResponse buscarPeloId(
            @Parameter(description = "Identificador do membro.", example = "1", required = true)
            @PathVariable Long id
    ) {
        return membroService.buscarPeloId(id);
    }

    @GetMapping
    @Operation(
            summary = "Listar membros",
            description = "Lista todos os membros cadastrados na API externa simulada."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Membros retornados.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MembroResponse.class)))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Autenticação obrigatória.",
            content = @Content
    )
    public List<MembroResponse> buscarTodos() {
        return membroService.buscarTodos();
    }
}