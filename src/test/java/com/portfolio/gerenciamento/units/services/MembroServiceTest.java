package com.portfolio.gerenciamento.units.services;

import com.portfolio.gerenciamento.dtos.request.CriarMembroRequest;
import com.portfolio.gerenciamento.dtos.response.MembroResponse;
import com.portfolio.gerenciamento.entities.Membro;
import com.portfolio.gerenciamento.enums.Atribuicao;
import com.portfolio.gerenciamento.exceptions.ResourceNotFoundException;
import com.portfolio.gerenciamento.mappers.MembroMapper;
import com.portfolio.gerenciamento.repositories.MembroRepository;
import com.portfolio.gerenciamento.services.MembroService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembroServiceTest {

    private static final Long MEMBRO_ID = 1L;
    private static final Long OUTRO_MEMBRO_ID = 2L;
    private static final Long MEMBRO_INEXISTENTE_ID = 99L;
    private static final String NOME_ANA = "Ana";
    private static final String NOME_BRUNO = "Bruno";
    private static final String MENSAGEM_MEMBRO_NAO_ENCONTRADO = "Membro não encontrado pelo id99";
    private static final LocalDateTime DATA_CRIACAO = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Mock
    private MembroRepository repository;

    @Mock
    private MembroMapper mapper;

    @InjectMocks
    private MembroService service;

    @Test
    @DisplayName("Deve salvar membro mapeado quando criar membro")
    void should_returnMappedResponse_when_memberIsCreated() {
        CriarMembroRequest request = new CriarMembroRequest(NOME_ANA, Atribuicao.FUNCIONARIO);
        Membro membro = membro(MEMBRO_ID, NOME_ANA, Atribuicao.FUNCIONARIO);
        MembroResponse response = response(MEMBRO_ID, NOME_ANA, Atribuicao.FUNCIONARIO);

        when(mapper.toEntity(request)).thenReturn(membro);
        when(repository.save(membro)).thenReturn(membro);
        when(mapper.toResponse(membro)).thenReturn(response);

        MembroResponse createdResponse = service.criar(request);

        assertThat(createdResponse).isSameAs(response);
        verify(repository).save(membro);
    }

    @Test
    @DisplayName("Deve retornar response quando buscar membro existente pelo id")
    void should_returnMappedResponse_when_memberExists() {
        Membro membro = membro(MEMBRO_ID, NOME_ANA, Atribuicao.FUNCIONARIO);
        MembroResponse response = response(MEMBRO_ID, NOME_ANA, Atribuicao.FUNCIONARIO);

        when(repository.findById(MEMBRO_ID)).thenReturn(Optional.of(membro));
        when(mapper.toResponse(membro)).thenReturn(response);

        MembroResponse foundResponse = service.buscarPeloId(MEMBRO_ID);

        assertThat(foundResponse).isSameAs(response);
    }

    @Test
    @DisplayName("Deve mapear todos os membros quando buscar todos")
    void should_returnAllMappedMembers_when_membersAreListed() {
        Membro ana = membro(MEMBRO_ID, NOME_ANA, Atribuicao.FUNCIONARIO);
        Membro bruno = membro(OUTRO_MEMBRO_ID, NOME_BRUNO, Atribuicao.GERENTE);
        MembroResponse anaResponse = response(MEMBRO_ID, NOME_ANA, Atribuicao.FUNCIONARIO);
        MembroResponse brunoResponse = response(OUTRO_MEMBRO_ID, NOME_BRUNO, Atribuicao.GERENTE);

        when(repository.findAll()).thenReturn(List.of(ana, bruno));
        when(mapper.toResponse(ana)).thenReturn(anaResponse);
        when(mapper.toResponse(bruno)).thenReturn(brunoResponse);

        List<MembroResponse> responses = service.buscarTodos();

        assertThat(responses).containsExactly(anaResponse, brunoResponse);
    }

    @Test
    @DisplayName("Deve lancar excecao quando buscar entidade de membro inexistente")
    void should_throwResourceNotFoundException_when_memberEntityDoesNotExist() {
        when(repository.findById(MEMBRO_INEXISTENTE_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.buscarEntidadePeloId(MEMBRO_INEXISTENTE_ID)
        );

        assertThat(exception.getMessage()).isEqualTo(MENSAGEM_MEMBRO_NAO_ENCONTRADO);
    }

    private static Membro membro(Long id, String nome, Atribuicao atribuicao) {
        return Membro.builder()
                .id(id)
                .nome(nome)
                .atribuicao(atribuicao)
                .build();
    }

    private static MembroResponse response(Long id, String nome, Atribuicao atribuicao) {
        return new MembroResponse(id, nome, atribuicao.name(), DATA_CRIACAO, null);
    }
}
