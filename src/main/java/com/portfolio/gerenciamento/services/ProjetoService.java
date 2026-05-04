package com.portfolio.gerenciamento.services;

import com.portfolio.gerenciamento.dtos.request.AtualizarProjetoRequest;
import com.portfolio.gerenciamento.dtos.request.AtualizarStatusProjetoRequest;
import com.portfolio.gerenciamento.dtos.request.CriarProjetoRequest;
import com.portfolio.gerenciamento.dtos.request.ProjetoFiltroRequest;
import com.portfolio.gerenciamento.dtos.response.ProjetoPageResponse;
import com.portfolio.gerenciamento.dtos.response.ProjetoResponse;
import com.portfolio.gerenciamento.entities.Membro;
import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.ClassificacaoRisco;
import com.portfolio.gerenciamento.exceptions.*;
import com.portfolio.gerenciamento.mappers.ProjetoMapper;
import com.portfolio.gerenciamento.repositories.ProjetoRepository;
import com.portfolio.gerenciamento.services.classificacaorisco.ClassificacaoRiscoService;
import com.portfolio.gerenciamento.services.validator.ProjetoMembroValidator;
import com.portfolio.gerenciamento.services.validator.ProjetoValidator;
import com.portfolio.gerenciamento.specification.ProjetoSpecifications;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Serviço responsável pelo gerenciamento de projetos.
 * <p>
 * Centraliza as operações de criação, consulta, atualização, exclusão
 * e gerenciamento de membros associados a projetos.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ProjetoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjetoService.class);

    private final ProjetoRepository repository;
    private final MembroService membroService;
    private final ProjetoMapper projetoMapper;
    private final ClassificacaoRiscoService classificacaoRiscoService;
    private final ProjetoValidator projetoValidator;
    private final ProjetoMembroValidator projetoMembroValidator;

    /**
     * Cria um novo projeto no sistema.
     * <p>
     * Realiza o mapeamento da requisição para entidade, associa o gerente responsável,
     * valida as datas e persiste o projeto.
     * </p>
     *
     * @param request objeto contendo os dados necessários para a criação do projeto
     * @return {@link ProjetoResponse} com os dados do projeto recém-criado
     * @throws ResourceNotFoundException caso o gerente informado não seja encontrado
     */
    @Transactional
    public ProjetoResponse criar(CriarProjetoRequest request) {
        Projeto projeto = projetoMapper.toEntity(request);
        Membro gerente = membroService.buscarEntidadePeloId(request.gerenteId());
        projetoValidator.validarGerente(gerente);
        projeto.setGerente(gerente);
        projetoValidator.validarDatas(projeto.getDataInicio(), projeto.getDataPrevistaFim(), projeto.getDataRealFim());
        Projeto projetoSalvo = repository.save(projeto);
        LOGGER.info("Projeto criado com id {}", projetoSalvo.getId());
        return toResponse(projetoSalvo);
    }

    /**
     * Busca um projeto pelo seu identificador único.
     *
     * @param id identificador único do projeto
     * @return {@link ProjetoResponse} com os dados do projeto encontrado
     * @throws ResourceNotFoundException caso nenhum projeto seja encontrado com o id informado
     */
    @Transactional(readOnly = true)
    public ProjetoResponse buscarPeloId(Long id) {
        return toResponse(buscarEntidadePeloId(id));
    }

    /**
     * Retorna uma página de projetos com base nos filtros informados.
     * <p>
     * Todos os parâmetros de filtro são opcionais. Quando a classificação de risco
     * é informada, a filtragem é realizada em memória após a consulta ao banco de dados,
     * pois a classificação é calculada dinamicamente.
     * </p>
     *
     * @param filtro   filtro para buscar dados do projeto
     * @param pageable configurações de paginação e ordenação
     * @return {@link ProjetoPageResponse} contendo os projetos encontrados e metadados de paginação
     */
    @Transactional(readOnly = true)
    public ProjetoPageResponse buscarTodos(ProjetoFiltroRequest filtro, Pageable pageable) {
        Specification<Projeto> specification = criarSpecification(filtro);

        Page<Projeto> pagina = filtro.classificacaoRisco() == null
                ? repository.findAll(specification, pageable)
                : filtrarPorClassificacaoDeRisco(specification, filtro.classificacaoRisco(), pageable);

        return toPageResponse(pagina);
    }

    /**
     * Atualiza os dados de um projeto existente.
     * <p>
     * Substitui todas as informações editáveis do projeto pelo conteúdo da requisição,
     * incluindo a reatribuição do gerente responsável, e revalida as datas.
     * </p>
     *
     * @param id      identificador único do projeto a ser atualizado
     * @param request objeto contendo os novos dados do projeto
     * @return {@link ProjetoResponse} com os dados atualizados do projeto
     * @throws ResourceNotFoundException caso o projeto ou o gerente informado não sejam encontrados
     */
    @Transactional
    public ProjetoResponse atualizar(Long id, AtualizarProjetoRequest request) {
        Projeto projeto = buscarEntidadePeloId(id);
        Membro gerente = membroService.buscarEntidadePeloId(request.gerenteId());
        projetoValidator.validarGerente(gerente);
        projeto.setNome(request.nome());
        projeto.setDataInicio(request.dataInicio());
        projeto.setDataPrevistaFim(request.dataPrevistaFim());
        projeto.setDataRealFim(request.dataRealFim());
        projeto.setOrcamentoTotal(request.orcamentoTotal());
        projeto.setDescricao(request.descricao());
        projeto.setGerente(gerente);
        projetoValidator.validarDatas(projeto.getDataInicio(), projeto.getDataPrevistaFim(), projeto.getDataRealFim());
        LOGGER.info("Projeto atualizado com id {}", projeto.getId());
        return toResponse(repository.save(projeto));
    }

    /**
     * Atualiza o status de um projeto existente.
     * <p>
     * Valida se a transição entre o status atual e o novo status é permitida
     * antes de realizar a atualização.
     * </p>
     *
     * @param id      identificador único do projeto
     * @param request objeto contendo o novo status desejado
     * @return {@link ProjetoResponse} com os dados atualizados do projeto
     * @throws ResourceNotFoundException        caso o projeto não seja encontrado
     * @throws InvalidStatusTransitionException caso a transição de status não seja permitida
     */
    @Transactional
    public ProjetoResponse atualizarStatus(Long id, AtualizarStatusProjetoRequest request) {
        Projeto projeto = buscarEntidadePeloId(id);
        projetoValidator.validarTransicaoStatus(projeto.getStatus(), request.status());
        projeto.setStatus(request.status());
        preencherDataRealFimQuandoFinalizado(projeto);
        LOGGER.info("Status do projeto id {} alterado para {}", projeto.getId(), request.status());
        return toResponse(repository.save(projeto));
    }

    /**
     * Remove um projeto do sistema pelo seu identificador único.
     * <p>
     * Valida se o projeto pode ser excluído antes de realizar a remoção.
     * </p>
     *
     * @param id identificador único do projeto a ser removido
     * @throws ResourceNotFoundException          caso o projeto não seja encontrado
     * @throws ProjectDeletionNotAllowedException caso o projeto esteja em um estado que não permite exclusão
     */
    @Transactional
    public void deletar(Long id) {
        Projeto projeto = buscarEntidadePeloId(id);
        projetoValidator.validarExclusao(projeto.getStatus());
        repository.delete(projeto);
        LOGGER.info("Projeto deletado com id {}", id);
    }

    /**
     * Associa um membro a um projeto existente.
     * <p>
     * Valida se a associação é permitida antes de adicioná-lo à lista de membros do projeto.
     * </p>
     *
     * @param projetoId identificador único do projeto
     * @param membroId  identificador único do membro a ser associado
     * @return {@link ProjetoResponse} com os dados atualizados do projeto
     * @throws ResourceNotFoundException      caso o projeto ou o membro não sejam encontrados
     * @throws InvalidProjectMemberException  caso a associação não seja permitida
     * @throws MemberAllocationLimitException casos se o projeto ou o membro atingirem o limite de alocações
     */
    @Transactional
    public ProjetoResponse adicionarMembro(Long projetoId, Long membroId) {
        Projeto projeto = buscarEntidadePeloId(projetoId);
        Membro membro = membroService.buscarEntidadePeloId(membroId);
        projetoMembroValidator.validarAssociacao(projeto, membro);
        projeto.getMembros().add(membro);
        LOGGER.info("Membro {} associado ao projeto {}", membroId, projetoId);
        return toResponse(repository.save(projeto));
    }

    /**
     * Remove um membro de um projeto existente.
     * <p>
     * Valida se a remoção é permitida antes de removê-lo da lista de membros do projeto.
     * </p>
     *
     * @param projetoId identificador único do projeto
     * @param membroId  identificador único do membro a ser removido
     * @return {@link ProjetoResponse} com os dados atualizados do projeto
     * @throws ResourceNotFoundException     caso o projeto ou o membro não sejam encontrados
     * @throws InvalidProjectMemberException caso a remoção não seja permitida
     */
    @Transactional
    public ProjetoResponse removerMembro(Long projetoId, Long membroId) {
        Projeto projeto = buscarEntidadePeloId(projetoId);
        Membro membro = membroService.buscarEntidadePeloId(membroId);
        projetoMembroValidator.validarRemocao(projeto, membro);
        projeto.getMembros().remove(membro);
        LOGGER.info("Membro {} removido do projeto {}", membroId, projetoId);
        return toResponse(repository.save(projeto));
    }

    /**
     * Busca a entidade {@link Projeto} pelo seu identificador único.
     * <p>
     * Utilizado internamente por outros métodos do serviço que necessitam
     * da entidade diretamente, em vez do DTO de resposta.
     * </p>
     *
     * @param id identificador único do projeto
     * @return entidade {@link Projeto} correspondente ao id informado
     * @throws ResourceNotFoundException caso nenhum projeto seja encontrado com o id informado
     */
    public Projeto buscarEntidadePeloId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado pelo id " + id));
    }

    /**
     * Filtra projetos em memória pela classificação de risco calculada e aplica paginação manual.
     * <p>
     * Necessário pois a classificação de risco é derivada dinamicamente a partir dos atributos
     * do projeto, não sendo possível filtrá-la diretamente via consulta ao banco de dados.
     * </p>
     *
     * @param specification      especificação com os demais filtros aplicados
     * @param classificacaoRisco classificação de risco desejada para filtragem
     * @param pageable           configurações de paginação e ordenação
     * @return {@link Page} contendo os projetos filtrados e paginados
     */
    private Page<Projeto> filtrarPorClassificacaoDeRisco(
            Specification<Projeto> specification,
            ClassificacaoRisco classificacaoRisco,
            Pageable pageable
    ) {
        List<Projeto> projetosFiltrados = repository.findAll(specification).stream()
                .filter(projeto -> classificacaoRiscoService.classificar(projeto) == classificacaoRisco)
                .toList();
        int inicio = (int) pageable.getOffset();
        int fim = Math.min(inicio + pageable.getPageSize(), projetosFiltrados.size());
        List<Projeto> conteudo = inicio >= projetosFiltrados.size() ? List.of() : projetosFiltrados.subList(inicio, fim);
        return new PageImpl<>(conteudo, pageable, projetosFiltrados.size());
    }

    /**
     * Converte uma entidade {@link Projeto} para {@link ProjetoResponse},
     * incluindo a classificação de risco calculada dinamicamente.
     *
     * @param projeto entidade a ser convertida
     * @return {@link ProjetoResponse} com os dados do projeto e sua classificação de risco
     */
    private ProjetoResponse toResponse(Projeto projeto) {
        return projetoMapper.toResponse(projeto, classificacaoRiscoService.classificar(projeto));
    }

    /**
     * Preenche a data real de fim do projeto quando este se encontra finalizado.
     *
     * <p>Um projeto é considerado finalizado quando seu status é:
     * <ul>
     *   <li>{@code ENCERRADO} - projeto concluído normalmente</li>
     *   <li>{@code CANCELADO} - projeto interrompido antes da conclusão</li>
     * </ul>
     *
     * <p>Nesses casos, a {@code dataRealFim} é preenchida automaticamente
     * com a data atual ({@link LocalDate#now()}), registrando o momento
     * em que o projeto deixou de estar ativo.
     *
     * <p>Se o projeto estiver em qualquer status ativo, nenhuma alteração é realizada.
     *
     * @param projeto o projeto a ser verificado e atualizado;
     *                não deve ser {@code null}
     */
    private void preencherDataRealFimQuandoFinalizado(Projeto projeto) {
        if (projetoValidator.validarSePodePreencherDataRealFim(projeto)) {
            projeto.setDataRealFim(LocalDate.now());
        }
    }

    /**
     * Cria uma {@link Specification} dinâmica para consulta de {@link Projeto}
     * com base nos filtros informados.
     * <p>
     * Cada critério é aplicado apenas quando o respectivo valor está presente no filtro,
     * permitindo composições flexíveis de busca.
     * </p>
     *
     * @param filtro objeto contendo os critérios opcionais de filtragem
     * @return {@link Specification} combinada para consulta de projetos
     */
    private Specification<Projeto> criarSpecification(ProjetoFiltroRequest filtro) {
        return Specification.allOf(
                ProjetoSpecifications.nomeContem(filtro.nome()),
                ProjetoSpecifications.temStatus(filtro.status()),
                ProjetoSpecifications.temGerente(filtro.gerenteId()),
                ProjetoSpecifications.iniciamEm(filtro.dataInicio()),
                ProjetoSpecifications.encerramPreviamenteEm(filtro.dataFimPrevista())
        );
    }

    /**
     * Converte uma {@link Page} de {@link Projeto} em um {@link ProjetoPageResponse}.
     * <p>
     * Realiza o mapeamento das entidades para DTOs de resposta e adiciona
     * os metadados de paginação necessários para o cliente.
     * </p>
     *
     * @param pagina página de projetos retornada pela camada de persistência
     * @return objeto de resposta contendo a lista de projetos e informações de paginação
     */
    private ProjetoPageResponse toPageResponse(Page<Projeto> pagina) {
        return new ProjetoPageResponse(
                pagina.getContent().stream()
                        .map(this::toResponse)
                        .toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }
}
