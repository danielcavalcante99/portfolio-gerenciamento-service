package com.portfolio.gerenciamento.integrations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.gerenciamento.enums.ProjetoStatus;
import com.portfolio.gerenciamento.repositories.MembroRepository;
import com.portfolio.gerenciamento.repositories.ProjetoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PortfolioApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private MembroRepository membroRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Deve buscar membro por id quando membro existir")
    void deveBuscarMembroPorIdQuandoMembroExistir() throws Exception {
        Long membroId = criarMembro("Ana Gerente", "GERENTE");

        ResultActions result = mockMvc.perform(get("/api/v1/membros/{id}", membroId)
                .with(basicAuth()));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(membroId))
                .andExpect(jsonPath("$.nome").value("Ana Gerente"))
                .andExpect(jsonPath("$.atribuicao").value("GERENTE"));
    }

    @Test
    @DisplayName("Deve listar todos os membros cadastrados")
    void deveListarTodosOsMembrosCadastrados() throws Exception {
        criarMembro("Ana Gerente", "GERENTE");
        criarMembro("Carla Funcionario", "FUNCIONARIO");

        ResultActions result = mockMvc.perform(get("/api/v1/membros")
                .with(basicAuth()));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Ana Gerente"))
                .andExpect(jsonPath("$[1].nome").value("Carla Funcionario"));
    }

    @Test
    @DisplayName("Deve retornar erro de validacao quando payload de membro for invalido")
    void deveRetornarErroDeValidacaoQuandoPayloadDeMembroForInvalido() throws Exception {
        ResultActions result = mockMvc.perform(post("/api/v1/membros")
                .with(basicAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "",
                          "atribuicao": null
                        }
                        """));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Bad Request"))
                .andExpect(jsonPath("$.mensagem").value("Falha na validação"))
                .andExpect(jsonPath("$.caminho").value("/api/v1/membros"))
                .andExpect(jsonPath("$.campoErros.nome").exists())
                .andExpect(jsonPath("$.campoErros.atribuicao").exists());
    }

    @Test
    @DisplayName("Deve retornar not found quando membro nao existir")
    void deveRetornarNotFoundQuandoMembroNaoExistir() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/membros/{id}", 999L)
                .with(basicAuth()));

        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Membro não encontrado pelo id999"))
                .andExpect(jsonPath("$.caminho").value("/api/v1/membros/999"));
    }

    @Test
    @DisplayName("Deve criar projeto quando gerente existir e payload for valido")
    void deveCriarProjetoQuandoGerenteExistirEPayloadForValido() throws Exception {
        Long gerenteId = criarMembro("Ana Gerente", "GERENTE");

        ResultActions result = mockMvc.perform(post("/api/v1/projetos")
                .with(basicAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(projetoJson("Projeto ERP", gerenteId, ProjetoStatus.EM_ANALISE)));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Projeto ERP"))
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.classificacaoRisco").value("MEDIO"));
        assertThat(projetoRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve retornar erro de validacao quando payload de projeto for invalido")
    void deveRetornarErroDeValidacaoQuandoPayloadDeProjetoForInvalido() throws Exception {
        ResultActions result = mockMvc.perform(post("/api/v1/projetos")
                .with(basicAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "",
                          "dataInicio": null,
                          "dataPrevistaFim": null,
                          "orcamentoTotal": -1,
                          "gerenteId": null,
                          "status": null
                        }
                        """));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Falha na validação"))
                .andExpect(jsonPath("$.campoErros.nome").exists())
                .andExpect(jsonPath("$.campoErros.dataInicio").exists())
                .andExpect(jsonPath("$.campoErros.dataPrevistaFim").exists())
                .andExpect(jsonPath("$.campoErros.orcamentoTotal").exists())
                .andExpect(jsonPath("$.campoErros.gerenteId").exists())
                .andExpect(jsonPath("$.campoErros.status").exists());
    }

    @Test
    @DisplayName("Deve buscar projeto por id quando projeto existir")
    void deveBuscarProjetoPorIdQuandoProjetoExistir() throws Exception {
        Long gerenteId = criarMembro("Ana Gerente", "GERENTE");
        Long membroId = criarMembro("Carla Funcionario", "FUNCIONARIO");
        Long projetoId = criarProjeto("Projeto ERP", gerenteId);
        associarMembro(projetoId, membroId);

        ResultActions result = mockMvc.perform(get("/api/v1/projetos/{id}", projetoId)
                .with(basicAuth()));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projetoId))
                .andExpect(jsonPath("$.nome").value("Projeto ERP"))
                .andExpect(jsonPath("$.gerente.id").value(gerenteId))
                .andExpect(jsonPath("$.membros.length()").value(1))
                .andExpect(jsonPath("$.membros[0].id").value(membroId));
    }

    @Test
    @DisplayName("Deve retornar not found quando projeto nao existir")
    void deveRetornarNotFoundQuandoProjetoNaoExistir() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/projetos/{id}", 999L)
                .with(basicAuth()));

        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Projeto não encontrado pelo id 999"))
                .andExpect(jsonPath("$.caminho").value("/api/v1/projetos/999"));
    }

    @Test
    @DisplayName("Deve atualizar projeto quando projeto e gerente existirem")
    void deveAtualizarProjetoQuandoProjetoEGerenteExistirem() throws Exception {
        Long gerenteId = criarMembro("Ana Gerente", "GERENTE");
        Long novoGerenteId = criarMembro("Bruno Gerente", "GERENTE");
        Long projetoId = criarProjeto("Projeto ERP", gerenteId);

        ResultActions result = mockMvc.perform(put("/api/v1/projetos/{id}", projetoId)
                .with(basicAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(atualizarProjetoJson("Projeto ERP Atualizado", novoGerenteId)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Projeto ERP Atualizado"))
                .andExpect(jsonPath("$.gerente.id").value(novoGerenteId));
        assertThat(projetoRepository.findById(projetoId)).hasValueSatisfying(projeto -> {
            assertThat(projeto.getNome()).isEqualTo("Projeto ERP Atualizado");
            assertThat(projeto.getGerente().getId()).isEqualTo(novoGerenteId);
        });
    }

    @Test
    @DisplayName("Deve alterar status quando transicao for valida")
    void deveAlterarStatusQuandoTransicaoForValida() throws Exception {
        Long gerenteId = criarMembro("Ana Gerente", "GERENTE");
        Long projetoId = criarProjeto("Projeto ERP", gerenteId);

        ResultActions result = mockMvc.perform(patch("/api/v1/projetos/{id}/status", projetoId)
                .with(basicAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "ANALISE_REALIZADA"
                        }
                        """));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ANALISE_REALIZADA"));
        assertThat(projetoRepository.findById(projetoId)).hasValueSatisfying(projeto ->
                assertThat(projeto.getStatus()).isEqualTo(ProjetoStatus.ANALISE_REALIZADA));
    }

    @Test
    @DisplayName("Deve rejeitar alteracao de status quando transicao for invalida")
    void deveRejeitarAlteracaoDeStatusQuandoTransicaoForInvalida() throws Exception {
        Long gerenteId = criarMembro("Ana Gerente", "GERENTE");
        Long projetoId = criarProjeto("Projeto ERP", gerenteId);

        ResultActions result = mockMvc.perform(patch("/api/v1/projetos/{id}/status", projetoId)
                .with(basicAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "INICIADO"
                        }
                        """));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Transição de status inválida de ANALISE_REALIZADA para INICIADO"));
        assertThat(projetoRepository.findById(projetoId)).hasValueSatisfying(projeto ->
                assertThat(projeto.getStatus()).isEqualTo(ProjetoStatus.ANALISE_REALIZADA));
    }

    @Test
    @DisplayName("Deve excluir projeto quando status permitir")
    void deveExcluirProjetoQuandoStatusPermitir() throws Exception {
        Long gerenteId = criarMembro("Ana Gerente", "GERENTE");
        Long projetoId = criarProjeto("Projeto ERP", gerenteId);

        ResultActions result = mockMvc.perform(delete("/api/v1/projetos/{id}", projetoId)
                .with(basicAuth()));

        result.andExpect(status().isNoContent());
        assertThat(projetoRepository.existsById(projetoId)).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar exclusao quando status nao permitir")
    void deveRejeitarExclusaoQuandoStatusNaoPermitir() throws Exception {
        Long gerenteId = criarMembro("Ana Gerente", "GERENTE");
        Long projetoId = criarProjeto("Projeto ERP", gerenteId);
        alterarStatus(projetoId, "ANALISE_REALIZADA");
        alterarStatus(projetoId, "ANALISE_APROVADA");
        alterarStatus(projetoId, "INICIADO");

        ResultActions result = mockMvc.perform(delete("/api/v1/projetos/{id}", projetoId)
                .with(basicAuth()));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O projeto não pode ser excluído com o status Iniciado"));
        assertThat(projetoRepository.existsById(projetoId)).isTrue();
    }

    @Test
    @DisplayName("Deve associar e remover membro quando regras de alocacao forem respeitadas")
    void deveAssociarERemoverMembroQuandoRegrasDeAlocacaoForemRespeitadas() throws Exception {
        Long gerenteId = criarMembro("Ana Gerente", "GERENTE");
        Long membroId = criarMembro("Carla Funcionario", "FUNCIONARIO");
        Long outroMembroId = criarMembro("Diego Funcionario", "FUNCIONARIO");
        Long projetoId = criarProjeto("Projeto ERP", gerenteId);
        associarMembro(projetoId, outroMembroId);

        ResultActions associacao = mockMvc.perform(post("/api/v1/projetos/{projetoId}/membros/{membroId}", projetoId, membroId)
                .with(basicAuth()));

        associacao.andExpect(status().isOk())
                .andExpect(jsonPath("$.membros.length()").value(2));
        assertThat(totalMembrosAlocados(projetoId)).isEqualTo(2);

        ResultActions remocao = mockMvc.perform(delete("/api/v1/projetos/{projetoId}/membros/{membroId}", projetoId, membroId)
                .with(basicAuth()));

        remocao.andExpect(status().isOk())
                .andExpect(jsonPath("$.membros.length()").value(1));
        assertThat(totalMembrosAlocados(projetoId)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve rejeitar associacao quando membro nao for funcionario")
    void deveRejeitarAssociacaoQuandoMembroNaoForFuncionario() throws Exception {
        Long gerenteId = criarMembro("Ana Gerente", "GERENTE");
        Long diretorId = criarMembro("Daniel Diretor", "DIRETOR");
        Long projetoId = criarProjeto("Projeto ERP", gerenteId);

        ResultActions result = mockMvc.perform(post("/api/v1/projetos/{projetoId}/membros/{membroId}", projetoId, diretorId)
                .with(basicAuth()));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Apenas membros com a atribuição de funcionário podem ser associados a projetos"));
        assertThat(totalMembrosAlocados(projetoId)).isZero();
    }

    @Test
    @DisplayName("Deve consultar projetos paginados quando filtros forem informados")
    void deveConsultarProjetosPaginadosQuandoFiltrosForemInformados() throws Exception {
        Long gerenteId = criarMembro("Ana Gerente", "GERENTE");
        criarProjeto("Projeto ERP", gerenteId);
        criarProjeto("Outro Produto", gerenteId);

        ResultActions result = mockMvc.perform(get("/api/v1/projetos")
                .with(basicAuth())
                .param("nome", "ERP")
                .param("status", "ANALISE_REALIZADA")
                .param("gerenteId", gerenteId.toString())
                .param("classificacaoRisco", "MEDIO")
                .param("page", "0")
                .param("size", "10"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()").value(1))
                .andExpect(jsonPath("$.conteudo[0].nome").value("Projeto ERP"))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    @DisplayName("Deve consultar projetos por filtros de data")
    void deveConsultarProjetosPorFiltrosDeData() throws Exception {
        Long gerenteId = criarMembro("Ana Gerente", "GERENTE");
        criarProjeto("Projeto ERP", gerenteId);

        ResultActions result = mockMvc.perform(get("/api/v1/projetos")
                .with(basicAuth())
                .param("dataInicio", "2026-01-01")
                .param("dataFimPrevista", "2026-07-01")
                .param("page", "0")
                .param("size", "10"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()").value(1))
                .andExpect(jsonPath("$.conteudo[0].nome").value("Projeto ERP"))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    @DisplayName("Deve gerar relatorio consolidado quando houver projetos e membros alocados")
    void deveGerarRelatorioConsolidadoQuandoHouverProjetosEMembrosAlocados() throws Exception {
        Long gerenteId = criarMembro("Ana Gerente", "GERENTE");
        Long membroId = criarMembro("Carla Funcionario", "FUNCIONARIO");
        Long projetoId = criarProjeto("Projeto ERP", gerenteId);
        associarMembro(projetoId, membroId);
        alterarStatus(projetoId, "ANALISE_REALIZADA");
        alterarStatus(projetoId, "ANALISE_APROVADA");
        alterarStatus(projetoId, "INICIADO");
        alterarStatus(projetoId, "EM_ANDAMENTO");
        alterarStatus(projetoId, "ENCERRADO");
        atualizarProjetoComDataReal(projetoId, gerenteId);

        ResultActions result = mockMvc.perform(get("/api/v1/relatorios/resumo-portfolio")
                .with(basicAuth()));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.contagemProjetosPorStatus.ENCERRADO").value(1))
                .andExpect(jsonPath("$.orcamentoTotalPorStatus.ENCERRADO").value(250000.00))
                .andExpect(jsonPath("$.mediaDuracaoProjetosEncerradosEmDias").value(181.0))
                .andExpect(jsonPath("$.totalMembrosUnicosAlocados").value(1));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("requisicoesProtegidas")
    @DisplayName("Deve exigir autenticacao nos endpoints protegidos")
    void deveExigirAutenticacaoNosEndpointsProtegidos(String descricao, MockHttpServletRequestBuilder request) throws Exception {
        ResultActions result = mockMvc.perform(request);

        result.andExpect(status().isUnauthorized());
    }

    private Long criarMembro(String nome, String atribuicao) throws Exception {
        String response = mockMvc.perform(post("/api/v1/membros")
                        .with(basicAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "atribuicao": "%s"
                                }
                                """.formatted(nome, atribuicao)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        Long id = json.get("id").asLong();
        assertThat(membroRepository.existsById(id)).isTrue();
        return id;
    }

    private Long criarProjeto(String nome, Long gerenteId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/projetos")
                        .with(basicAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projetoJson(nome, gerenteId, ProjetoStatus.ANALISE_REALIZADA)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        Long id = json.get("id").asLong();
        assertThat(projetoRepository.existsById(id)).isTrue();
        return id;
    }

    private void alterarStatus(Long projetoId, String novoStatus) throws Exception {
        mockMvc.perform(patch("/api/v1/projetos/{id}/status", projetoId)
                        .with(basicAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "%s"
                                }
                                """.formatted(novoStatus)))
                .andExpect(status().isOk());
    }

    private void associarMembro(Long projetoId, Long membroId) throws Exception {
        mockMvc.perform(post("/api/v1/projetos/{projetoId}/membros/{membroId}", projetoId, membroId)
                        .with(basicAuth()))
                .andExpect(status().isOk());
    }

    private void atualizarProjetoComDataReal(Long projetoId, Long gerenteId) throws Exception {
        mockMvc.perform(put("/api/v1/projetos/{id}", projetoId)
                        .with(basicAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Projeto ERP",
                                  "dataInicio": "2026-01-01",
                                  "dataPrevistaFim": "2026-07-01",
                                  "dataRealFim": "2026-07-01",
                                  "orcamentoTotal": 250000.00,
                                  "descricao": "Projeto encerrado",
                                  "gerenteId": %d
                                }
                                """.formatted(gerenteId)))
                .andExpect(status().isOk());
    }

    private Integer totalMembrosAlocados(Long projetoId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from projeto_membros where projeto_id = ?",
                Integer.class,
                projetoId
        );
    }

    private static String projetoJson(String nome, Long gerenteId, ProjetoStatus status) {
        return """
                {
                  "nome": "%s",
                  "dataInicio": "2026-01-01",
                  "dataPrevistaFim": "2026-07-01",
                  "dataRealFim": null,
                  "orcamentoTotal": 250000.00,
                  "descricao": "Projeto de integracao",
                  "gerenteId": %d,
                  "status": "%s"
                }
                """.formatted(nome, gerenteId, status);
    }

    private static String atualizarProjetoJson(String nome, Long gerenteId) {
        return """
                {
                  "nome": "%s",
                  "dataInicio": "2026-02-01",
                  "dataPrevistaFim": "2026-08-01",
                  "dataRealFim": null,
                  "orcamentoTotal": 300000.00,
                  "descricao": "Projeto atualizado",
                  "gerenteId": %d
                }
                """.formatted(nome, gerenteId);
    }

    private static Stream<Arguments> requisicoesProtegidas() {
        return Stream.of(
                arguments("POST /api/v1/membros", post("/api/v1/membros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Gerente",
                                  "atribuicao": "GERENTE"
                                }
                                """)),
                arguments("GET /api/v1/membros/{id}", get("/api/v1/membros/{id}", 1L)),
                arguments("GET /api/v1/membros", get("/api/v1/membros")),
                arguments("POST /api/v1/projetos", post("/api/v1/projetos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projetoJson("Projeto ERP", 1L, ProjetoStatus.EM_ANALISE))),
                arguments("GET /api/v1/projetos/{id}", get("/api/v1/projetos/{id}", 1L)),
                arguments("GET /api/v1/projetos", get("/api/v1/projetos")),
                arguments("PUT /api/v1/projetos/{id}", put("/api/v1/projetos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizarProjetoJson("Projeto ERP Atualizado", 1L))),
                arguments("PATCH /api/v1/projetos/{id}/status", patch("/api/v1/projetos/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "ANALISE_REALIZADA"
                                }
                                """)),
                arguments("DELETE /api/v1/projetos/{id}", delete("/api/v1/projetos/{id}", 1L)),
                arguments("POST /api/v1/projetos/{projetoId}/membros/{membroId}",
                        post("/api/v1/projetos/{projetoId}/membros/{membroId}", 1L, 1L)),
                arguments("DELETE /api/v1/projetos/{projetoId}/membros/{membroId}",
                        delete("/api/v1/projetos/{projetoId}/membros/{membroId}", 1L, 1L)),
                arguments("GET /api/v1/relatorios/resumo-portfolio", get("/api/v1/relatorios/resumo-portfolio"))
        );
    }
}
