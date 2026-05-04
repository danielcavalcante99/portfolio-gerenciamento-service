package com.portfolio.gerenciamento.units.services;

import com.portfolio.gerenciamento.dtos.request.AtualizarProjetoRequest;
import com.portfolio.gerenciamento.dtos.request.AtualizarStatusProjetoRequest;
import com.portfolio.gerenciamento.dtos.request.CriarProjetoRequest;
import com.portfolio.gerenciamento.dtos.request.ProjetoFiltroRequest;
import com.portfolio.gerenciamento.dtos.response.ProjetoPageResponse;
import com.portfolio.gerenciamento.dtos.response.ProjetoResponse;
import com.portfolio.gerenciamento.entities.Membro;
import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.Atribuicao;
import com.portfolio.gerenciamento.enums.ClassificacaoRisco;
import com.portfolio.gerenciamento.enums.ProjetoStatus;
import com.portfolio.gerenciamento.exceptions.InvalidProjectManagerException;
import com.portfolio.gerenciamento.exceptions.ResourceNotFoundException;
import com.portfolio.gerenciamento.mappers.ProjetoMapper;
import com.portfolio.gerenciamento.repositories.ProjetoRepository;
import com.portfolio.gerenciamento.services.MembroService;
import com.portfolio.gerenciamento.services.ProjetoService;
import com.portfolio.gerenciamento.services.classificacaorisco.ClassificacaoRiscoService;
import com.portfolio.gerenciamento.services.validator.ProjetoMembroValidator;
import com.portfolio.gerenciamento.services.validator.ProjetoValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock
    private ProjetoRepository repository;

    @Mock
    private MembroService membroService;

    @Mock
    private ProjetoMapper projetoMapper;

    @Mock
    private ClassificacaoRiscoService classificacaoRiscoService;

    @Mock
    private ProjetoValidator projetoValidator;

    @Mock
    private ProjetoMembroValidator projetoMembroValidator;

    @InjectMocks
    private ProjetoService service;

    @Test
    @DisplayName("Deve criar projeto quando dados forem validos")
    void should_returnResponse_when_projectIsCreatedWithValidData() {
        CriarProjetoRequest request = criarRequest();
        Projeto projeto = projeto(1L, ProjetoStatus.EM_ANALISE);
        ProjetoResponse response = response(1L);
        Membro gerente = membro(10L);

        when(projetoMapper.toEntity(request)).thenReturn(projeto);
        when(membroService.buscarEntidadePeloId(10L)).thenReturn(gerente);
        when(repository.save(projeto)).thenReturn(projeto);
        when(classificacaoRiscoService.classificar(projeto)).thenReturn(ClassificacaoRisco.BAIXO);
        when(projetoMapper.toResponse(projeto, ClassificacaoRisco.BAIXO)).thenReturn(response);

        assertSame(response, service.criar(request));
        assertSame(gerente, projeto.getGerente());
        verify(projetoValidator).validarGerente(gerente);
        verify(projetoValidator).validarDatas(projeto.getDataInicio(), projeto.getDataPrevistaFim(), projeto.getDataRealFim());
        verify(repository).save(projeto);
    }

    @Test
    @DisplayName("Deve rejeitar criacao de projeto quando gerente nao tiver atribuicao gerente")
    void should_throwInvalidProjectManagerException_when_projectManagerIsInvalidOnCreate() {
        CriarProjetoRequest request = criarRequest();
        Projeto projeto = projeto(1L, ProjetoStatus.EM_ANALISE);
        Membro gerente = membro(10L);

        when(projetoMapper.toEntity(request)).thenReturn(projeto);
        when(membroService.buscarEntidadePeloId(10L)).thenReturn(gerente);
        doThrow(new InvalidProjectManagerException("O gerente do projeto deve possuir a atribuição GERENTE"))
                .when(projetoValidator).validarGerente(gerente);

        assertThrows(InvalidProjectManagerException.class, () -> service.criar(request));

        verify(repository, never()).save(any(Projeto.class));
    }

    @Test
    @DisplayName("Deve retornar response quando buscar projeto existente pelo id")
    void should_returnResponse_when_projectExists() {
        Projeto projeto = projeto(1L, ProjetoStatus.EM_ANALISE);
        ProjetoResponse response = response(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(projeto));
        when(classificacaoRiscoService.classificar(projeto)).thenReturn(ClassificacaoRisco.MEDIO);
        when(projetoMapper.toResponse(projeto, ClassificacaoRisco.MEDIO)).thenReturn(response);

        assertSame(response, service.buscarPeloId(1L));
    }

    @Test
    @DisplayName("Deve lancar excecao quando buscar entidade de projeto inexistente")
    void should_throwResourceNotFoundException_when_projectEntityDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.buscarEntidadePeloId(99L)
        );

        assertEquals("Projeto não encontrado pelo id 99", exception.getMessage());
    }

    @Test
    @DisplayName("Deve usar consulta paginada do repositorio quando filtro de risco nao for informado")
    void should_usePagedRepositoryQuery_when_riskFilterIsNotProvided() {
        Projeto projeto = projeto(1L, ProjetoStatus.EM_ANALISE);
        ProjetoResponse response = response(1L);
        PageRequest pageable = PageRequest.of(0, 5);
        ProjetoFiltroRequest filtro = filtro(null);

        when(repository.findAll(anySpecification(), eq(pageable))).thenReturn(new PageImpl<>(List.of(projeto), pageable, 1));
        when(classificacaoRiscoService.classificar(projeto)).thenReturn(ClassificacaoRisco.BAIXO);
        when(projetoMapper.toResponse(projeto, ClassificacaoRisco.BAIXO)).thenReturn(response);

        ProjetoPageResponse page = service.buscarTodos(filtro, pageable);

        assertEquals(List.of(response), page.conteudo());
        assertEquals(0, page.pagina());
        assertEquals(5, page.tamanho());
        assertEquals(1, page.totalElementos());
        assertEquals(1, page.totalPaginas());
    }

    @Test
    @DisplayName("Deve filtrar em memoria quando filtro de risco for informado")
    void should_filterInMemoryAndPageResult_when_riskFilterIsProvided() {
        Projeto baixo = projeto(1L, ProjetoStatus.EM_ANALISE);
        Projeto medio = projeto(2L, ProjetoStatus.EM_ANDAMENTO);
        ProjetoResponse response = response(2L);
        PageRequest pageable = PageRequest.of(0, 1);

        when(repository.findAll(anySpecification())).thenReturn(List.of(baixo, medio));
        when(classificacaoRiscoService.classificar(baixo)).thenReturn(ClassificacaoRisco.BAIXO);
        when(classificacaoRiscoService.classificar(medio)).thenReturn(ClassificacaoRisco.MEDIO);
        when(projetoMapper.toResponse(medio, ClassificacaoRisco.MEDIO)).thenReturn(response);

        ProjetoPageResponse page = service.buscarTodos(filtro(ClassificacaoRisco.MEDIO), pageable);

        assertEquals(List.of(response), page.conteudo());
        assertEquals(1, page.totalElementos());
        assertEquals(1, page.totalPaginas());
    }

    @Test
    @DisplayName("Deve retornar pagina vazia quando offset passar do total filtrado por risco")
    void should_returnEmptyPage_when_riskFilterOffsetExceedsTotal() {
        Projeto medio = projeto(2L, ProjetoStatus.EM_ANDAMENTO);
        PageRequest pageable = PageRequest.of(2, 1);

        when(repository.findAll(anySpecification())).thenReturn(List.of(medio));
        when(classificacaoRiscoService.classificar(medio)).thenReturn(ClassificacaoRisco.MEDIO);

        ProjetoPageResponse page = service.buscarTodos(filtro(ClassificacaoRisco.MEDIO), pageable);

        assertThat(page.conteudo()).isEmpty();
        assertEquals(1, page.totalElementos());
    }

    @Test
    @DisplayName("Deve atualizar projeto quando dados forem validos")
    void should_returnResponseAndUpdateFields_when_projectIsUpdatedWithValidData() {
        Projeto projeto = projeto(1L, ProjetoStatus.EM_ANALISE);
        Membro gerente = membro(20L);
        AtualizarProjetoRequest request = atualizarRequest();
        ProjetoResponse response = response(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(projeto));
        when(membroService.buscarEntidadePeloId(20L)).thenReturn(gerente);
        when(repository.save(projeto)).thenReturn(projeto);
        when(classificacaoRiscoService.classificar(projeto)).thenReturn(ClassificacaoRisco.ALTO);
        when(projetoMapper.toResponse(projeto, ClassificacaoRisco.ALTO)).thenReturn(response);

        assertSame(response, service.atualizar(1L, request));
        assertEquals("Projeto atualizado", projeto.getNome());
        assertEquals(LocalDate.of(2026, 2, 1), projeto.getDataInicio());
        assertEquals(LocalDate.of(2026, 7, 1), projeto.getDataPrevistaFim());
        assertEquals(LocalDate.of(2026, 8, 1), projeto.getDataRealFim());
        assertEquals(BigDecimal.valueOf(300_000), projeto.getOrcamentoTotal());
        assertEquals("Descricao atualizada", projeto.getDescricao());
        assertSame(gerente, projeto.getGerente());
        verify(projetoValidator).validarGerente(gerente);
        verify(projetoValidator).validarDatas(projeto.getDataInicio(), projeto.getDataPrevistaFim(), projeto.getDataRealFim());
    }

    @Test
    @DisplayName("Deve rejeitar atualizacao de projeto quando gerente nao tiver atribuicao gerente")
    void should_throwInvalidProjectManagerException_when_projectManagerIsInvalidOnUpdate() {
        Projeto projeto = projeto(1L, ProjetoStatus.EM_ANALISE);
        Membro gerente = membro(20L);
        AtualizarProjetoRequest request = atualizarRequest();

        when(repository.findById(1L)).thenReturn(Optional.of(projeto));
        when(membroService.buscarEntidadePeloId(20L)).thenReturn(gerente);
        doThrow(new InvalidProjectManagerException("O gerente do projeto deve possuir a atribuição GERENTE"))
                .when(projetoValidator).validarGerente(gerente);

        assertThrows(InvalidProjectManagerException.class, () -> service.atualizar(1L, request));

        verify(repository, never()).save(any(Projeto.class));
    }

    @Test
    @DisplayName("Deve atualizar status quando transicao for valida")
    void should_returnResponseAndChangeStatus_when_statusTransitionIsValid() {
        Projeto projeto = projeto(1L, ProjetoStatus.EM_ANALISE);
        ProjetoResponse response = response(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(projeto));
        when(repository.save(projeto)).thenReturn(projeto);
        when(classificacaoRiscoService.classificar(projeto)).thenReturn(ClassificacaoRisco.BAIXO);
        when(projetoMapper.toResponse(projeto, ClassificacaoRisco.BAIXO)).thenReturn(response);

        assertSame(response, service.atualizarStatus(1L, new AtualizarStatusProjetoRequest(ProjetoStatus.ANALISE_REALIZADA)));
        assertEquals(ProjetoStatus.ANALISE_REALIZADA, projeto.getStatus());
        verify(projetoValidator).validarTransicaoStatus(ProjetoStatus.EM_ANALISE, ProjetoStatus.ANALISE_REALIZADA);
    }

    @Test
    @DisplayName("Deve preencher data real fim quando status for encerrado")
    void should_fillActualEndDate_when_statusIsClosed() {
        Projeto projeto = projeto(1L, ProjetoStatus.EM_ANDAMENTO);
        projeto.setDataRealFim(null);
        ProjetoResponse response = response(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(projeto));
        when(projetoValidator.validarSePodePreencherDataRealFim(projeto)).thenReturn(true);
        when(repository.save(projeto)).thenReturn(projeto);
        when(classificacaoRiscoService.classificar(projeto)).thenReturn(ClassificacaoRisco.BAIXO);
        when(projetoMapper.toResponse(projeto, ClassificacaoRisco.BAIXO)).thenReturn(response);

        service.atualizarStatus(1L, new AtualizarStatusProjetoRequest(ProjetoStatus.ENCERRADO));

        assertEquals(ProjetoStatus.ENCERRADO, projeto.getStatus());
        assertThat(projeto.getDataRealFim()).isNotNull();
        assertThat(projeto.getDataRealFim()).isAfterOrEqualTo(projeto.getDataInicio());
        verify(projetoValidator).validarSePodePreencherDataRealFim(projeto);
    }

    @Test
    @DisplayName("Deve preencher data real fim quando status for cancelado")
    void should_fillActualEndDate_when_statusIsCanceled() {
        Projeto projeto = projeto(1L, ProjetoStatus.EM_ANALISE);
        projeto.setDataRealFim(null);
        ProjetoResponse response = response(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(projeto));
        when(projetoValidator.validarSePodePreencherDataRealFim(projeto)).thenReturn(true);
        when(repository.save(projeto)).thenReturn(projeto);
        when(classificacaoRiscoService.classificar(projeto)).thenReturn(ClassificacaoRisco.BAIXO);
        when(projetoMapper.toResponse(projeto, ClassificacaoRisco.BAIXO)).thenReturn(response);

        service.atualizarStatus(1L, new AtualizarStatusProjetoRequest(ProjetoStatus.CANCELADO));

        assertEquals(ProjetoStatus.CANCELADO, projeto.getStatus());
        assertThat(projeto.getDataRealFim()).isNotNull();
        assertThat(projeto.getDataRealFim()).isAfterOrEqualTo(projeto.getDataInicio());
        verify(projetoValidator).validarSePodePreencherDataRealFim(projeto);
    }

    @Test
    @DisplayName("Nao deve preencher data real fim quando validacao rejeitar")
    void should_keepActualEndDate_when_fillValidationRejects() {
        Projeto projeto = projeto(1L, ProjetoStatus.EM_ANDAMENTO);
        LocalDate dataRealFim = LocalDate.of(2026, 8, 1);
        projeto.setDataRealFim(dataRealFim);
        ProjetoResponse response = response(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(projeto));
        when(projetoValidator.validarSePodePreencherDataRealFim(projeto)).thenReturn(false);
        when(repository.save(projeto)).thenReturn(projeto);
        when(classificacaoRiscoService.classificar(projeto)).thenReturn(ClassificacaoRisco.BAIXO);
        when(projetoMapper.toResponse(projeto, ClassificacaoRisco.BAIXO)).thenReturn(response);

        service.atualizarStatus(1L, new AtualizarStatusProjetoRequest(ProjetoStatus.ENCERRADO));

        assertEquals(ProjetoStatus.ENCERRADO, projeto.getStatus());
        assertEquals(dataRealFim, projeto.getDataRealFim());
        verify(projetoValidator).validarSePodePreencherDataRealFim(projeto);
    }

    @Test
    @DisplayName("Deve deletar projeto quando exclusao for permitida")
    void should_deleteProject_when_deletionIsAllowed() {
        Projeto projeto = projeto(1L, ProjetoStatus.EM_ANALISE);
        when(repository.findById(1L)).thenReturn(Optional.of(projeto));

        service.deletar(1L);

        verify(projetoValidator).validarExclusao(ProjetoStatus.EM_ANALISE);
        verify(repository).delete(projeto);
    }

    @Test
    @DisplayName("Deve adicionar membro quando associacao for valida")
    void should_addMemberAndReturnResponse_when_associationIsValid() {
        Projeto projeto = projeto(1L, ProjetoStatus.EM_ANALISE);
        Membro membro = membro(2L);
        ProjetoResponse response = response(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(projeto));
        when(membroService.buscarEntidadePeloId(2L)).thenReturn(membro);
        when(repository.save(projeto)).thenReturn(projeto);
        when(classificacaoRiscoService.classificar(projeto)).thenReturn(ClassificacaoRisco.BAIXO);
        when(projetoMapper.toResponse(projeto, ClassificacaoRisco.BAIXO)).thenReturn(response);

        assertSame(response, service.adicionarMembro(1L, 2L));
        assertThat(projeto.getMembros()).contains(membro);
        verify(projetoMembroValidator).validarAssociacao(projeto, membro);
    }

    @Test
    @DisplayName("Deve remover membro quando remocao for valida")
    void should_removeMemberAndReturnResponse_when_removalIsValid() {
        Membro membro = membro(2L);
        Projeto projeto = projeto(1L, ProjetoStatus.EM_ANALISE);
        projeto.getMembros().add(membro);
        ProjetoResponse response = response(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(projeto));
        when(membroService.buscarEntidadePeloId(2L)).thenReturn(membro);
        when(repository.save(projeto)).thenReturn(projeto);
        when(classificacaoRiscoService.classificar(projeto)).thenReturn(ClassificacaoRisco.BAIXO);
        when(projetoMapper.toResponse(projeto, ClassificacaoRisco.BAIXO)).thenReturn(response);

        assertSame(response, service.removerMembro(1L, 2L));
        assertThat(projeto.getMembros()).isEmpty();
        verify(projetoMembroValidator).validarRemocao(projeto, membro);
    }

    @SuppressWarnings("unchecked")
    private static Specification<Projeto> anySpecification() {
        return any(Specification.class);
    }

    private static CriarProjetoRequest criarRequest() {
        return new CriarProjetoRequest(
                "Projeto",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 1),
                null,
                BigDecimal.valueOf(100_000),
                "Descricao",
                10L,
                ProjetoStatus.EM_ANALISE
        );
    }

    private static AtualizarProjetoRequest atualizarRequest() {
        return new AtualizarProjetoRequest(
                "Projeto atualizado",
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 8, 1),
                BigDecimal.valueOf(300_000),
                "Descricao atualizada",
                20L
        );
    }

    private static ProjetoFiltroRequest filtro(ClassificacaoRisco classificacaoRisco) {
        return new ProjetoFiltroRequest(
                "Proj",
                ProjetoStatus.EM_ANALISE,
                10L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 1),
                classificacaoRisco
        );
    }

    private static Projeto projeto(Long id, ProjetoStatus status) {
        return Projeto.builder()
                .id(id)
                .nome("Projeto")
                .dataInicio(LocalDate.of(2026, 1, 1))
                .dataPrevistaFim(LocalDate.of(2026, 6, 1))
                .orcamentoTotal(BigDecimal.valueOf(100_000))
                .descricao("Descricao")
                .status(status)
                .membros(new HashSet<>())
                .build();
    }

    private static Membro membro(Long id) {
        return Membro.builder()
                .id(id)
                .nome("Membro " + id)
                .atribuicao(Atribuicao.FUNCIONARIO)
                .build();
    }

    private static ProjetoResponse response(Long id) {
        return new ProjetoResponse(
                id,
                "Projeto",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 1),
                null,
                BigDecimal.valueOf(100_000),
                "Descricao",
                null,
                ProjetoStatus.EM_ANALISE,
                ClassificacaoRisco.BAIXO,
                java.util.Set.of(),
                null,
                null
        );
    }
}
