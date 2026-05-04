package com.portfolio.gerenciamento.services;

import com.portfolio.gerenciamento.dtos.request.CriarMembroRequest;
import com.portfolio.gerenciamento.dtos.response.MembroResponse;
import com.portfolio.gerenciamento.entities.Membro;
import com.portfolio.gerenciamento.exceptions.ResourceNotFoundException;
import com.portfolio.gerenciamento.mappers.MembroMapper;
import com.portfolio.gerenciamento.repositories.MembroRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço responsável pela gestão de membros no sistema.
 * <p>
 * Esta classe atua como camada de aplicação, orquestrando operações de criação
 * e consulta de {@link Membro}, delegando a persistência ao {@link MembroRepository}
 * e a conversão entre entidades e DTOs ao {@link MembroMapper}.
 * </p>
 *
 * <p>
 * As operações são transacionais, garantindo consistência dos dados e isolamento
 * adequado conforme o tipo de operação (leitura ou escrita).
 * </p>
 */
@Service
@RequiredArgsConstructor
public class MembroService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MembroService.class);

    private final MembroRepository repository;
    private final MembroMapper mapper;

    /**
     * Cria um novo membro no sistema.
     *
     * @param request objeto contendo os dados necessários para criação do membro
     * @return {@link MembroResponse} com os dados do membro recém-criado
     */
    @Transactional
    public MembroResponse criar(CriarMembroRequest request) {
        Membro membro = repository.save(mapper.toEntity(request));
        LOGGER.info("Membro criado com id {}", membro.getId());
        return mapper.toResponse(membro);
    }

    /**
     * Busca um membro pelo seu identificador único.
     *
     * @param id identificador único do membro
     * @return {@link MembroResponse} com os dados do membro encontrado
     * @throws ResourceNotFoundException caso nenhum membro seja encontrado com o id informado
     */
    @Transactional(readOnly = true)
    public MembroResponse buscarPeloId(Long id) {
        return mapper.toResponse(buscarEntidadePeloId(id));
    }

    /**
     * Retorna uma lista com todos os membros cadastrados no sistema.
     *
     * @return lista de {@link MembroResponse} com os dados de todos os membros
     */
    @Transactional
    public List<MembroResponse> buscarTodos() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Busca a entidade {@link Membro} pelo seu identificador único.
     * <p>
     * Utilizado internamente para operações que necessitam da entidade,
     * em vez do DTO de resposta.
     * </p>
     *
     * @param id identificador único do membro
     * @return entidade {@link Membro} correspondente ao id informado
     * @throws ResourceNotFoundException caso nenhum membro seja encontrado com o id informado
     */
     public Membro buscarEntidadePeloId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado pelo id" + id));
    }
}
