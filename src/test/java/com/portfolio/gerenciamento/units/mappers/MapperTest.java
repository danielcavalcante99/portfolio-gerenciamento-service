package com.portfolio.gerenciamento.units.mappers;

import com.portfolio.gerenciamento.dtos.request.CriarMembroRequest;
import com.portfolio.gerenciamento.dtos.request.CriarProjetoRequest;
import com.portfolio.gerenciamento.dtos.response.ProjetoResponse;
import com.portfolio.gerenciamento.entities.Membro;
import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.Atribuicao;
import com.portfolio.gerenciamento.enums.ClassificacaoRisco;
import com.portfolio.gerenciamento.enums.ProjetoStatus;
import com.portfolio.gerenciamento.mappers.MembroMapper;
import com.portfolio.gerenciamento.mappers.ProjetoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    private final MembroMapper membroMapper = Mappers.getMapper(MembroMapper.class);
    private final ProjetoMapper projetoMapper = projetoMapper();

    @Test
    @DisplayName("Deve mapear request de membro para entidade")
    void deveMapearRequestDeMembroParaEntidade() {
        Membro membro = membroMapper.toEntity(new CriarMembroRequest("Ana", Atribuicao.FUNCIONARIO));

        assertThat(membro.getNome()).isEqualTo("Ana");
        assertThat(membro.getAtribuicao()).isEqualTo(Atribuicao.FUNCIONARIO);
    }

    @Test
    @DisplayName("Deve mapear membro para response")
    void deveMapearMembroParaResponse() {
        Membro membro = Membro.builder()
                .id(1L)
                .nome("Ana")
                .atribuicao(Atribuicao.GERENTE)
                .build();

        assertThat(membroMapper.toResponse(membro))
                .extracting("id", "nome", "atribuicao")
                .containsExactly(1L, "Ana", Atribuicao.GERENTE.name());
    }

    @Test
    @DisplayName("Deve retornar null quando entrada de membro for null")
    void deveRetornarNullQuandoEntradaDeMembroForNull() {
        assertThat(membroMapper.toEntity(null)).isNull();
        assertThat(membroMapper.toResponse(null)).isNull();
    }

    @Test
    @DisplayName("Deve mapear membro com atribuicao nula preservando campo nulo")
    void deveMapearMembroComAtribuicaoNulaPreservandoCampoNulo() {
        Membro entity = membroMapper.toEntity(new CriarMembroRequest("Ana", null));
        Membro responseSource = Membro.builder()
                .id(1L)
                .nome("Ana")
                .atribuicao(null)
                .build();

        assertThat(entity.getAtribuicao()).isNull();
        assertThat(membroMapper.toResponse(responseSource).atribuicao()).isNull();
    }

    @Test
    @DisplayName("Deve mapear request de projeto para entidade ignorando relacionamentos")
    void deveMapearRequestDeProjetoParaEntidadeIgnorandoRelacionamentos() {
        CriarProjetoRequest request = new CriarProjetoRequest(
                "Projeto",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 7, 1),
                null,
                BigDecimal.valueOf(250_000),
                "Descricao",
                1L,
                ProjetoStatus.EM_ANALISE
        );

        Projeto projeto = projetoMapper.toEntity(request);

        assertThat(projeto.getNome()).isEqualTo("Projeto");
        assertThat(projeto.getDataInicio()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(projeto.getDataPrevistaFim()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(projeto.getOrcamentoTotal()).isEqualByComparingTo("250000");
        assertThat(projeto.getDescricao()).isEqualTo("Descricao");
        assertThat(projeto.getStatus()).isEqualTo(ProjetoStatus.EM_ANALISE);
        assertThat(projeto.getGerente()).isNull();
        assertThat(projeto.getMembros()).isNull();
    }

    @Test
    @DisplayName("Deve mapear projeto para response com gerente, membros e risco")
    void deveMapearProjetoParaResponseComGerenteMembrosERisco() {
        Membro gerente = membro(1L, "Ana", Atribuicao.GERENTE);
        Membro funcionario = membro(2L, "Carla", Atribuicao.FUNCIONARIO);
        Projeto projeto = Projeto.builder()
                .id(10L)
                .nome("Projeto")
                .dataInicio(LocalDate.of(2026, 1, 1))
                .dataPrevistaFim(LocalDate.of(2026, 7, 1))
                .orcamentoTotal(BigDecimal.valueOf(250_000))
                .descricao("Descricao")
                .gerente(gerente)
                .status(ProjetoStatus.EM_ANALISE)
                .membros(Set.of(funcionario))
                .build();

        ProjetoResponse response = projetoMapper.toResponse(projeto, ClassificacaoRisco.MEDIO);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.gerente().id()).isEqualTo(1L);
        assertThat(response.classificacaoRisco()).isEqualTo(ClassificacaoRisco.MEDIO);
        assertThat(response.membros()).extracting("id").containsExactly(2L);
    }

    @Test
    @DisplayName("Deve lidar com entradas nulas no mapper de projeto")
    void deveLidarComEntradasNulasNoMapperDeProjeto() {
        assertThat(projetoMapper.toEntity(null)).isNull();
        assertThat(projetoMapper.toResponse(null, null)).isNull();
        assertThat(projetoMapper.toResponse(null, ClassificacaoRisco.ALTO).classificacaoRisco())
                .isEqualTo(ClassificacaoRisco.ALTO);
        assertThat(projetoMapper.toResponse(Projeto.builder().build(), ClassificacaoRisco.BAIXO).membros())
                .isNull();
    }

    private ProjetoMapper projetoMapper() {
        ProjetoMapper mapper = Mappers.getMapper(ProjetoMapper.class);
        ReflectionTestUtils.setField(mapper, "membroMapper", membroMapper);
        return mapper;
    }

    private static Membro membro(Long id, String nome, Atribuicao atribuicao) {
        return Membro.builder()
                .id(id)
                .nome(nome)
                .atribuicao(atribuicao)
                .build();
    }
}
