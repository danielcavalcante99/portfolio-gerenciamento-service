package com.portfolio.gerenciamento.units.specification;

import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.ProjetoStatus;
import com.portfolio.gerenciamento.specification.ProjetoSpecifications;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetoSpecificationsTest {

    @Mock
    private Root<Projeto> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder builder;

    @Mock
    private Predicate predicate;

    @Mock
    private Predicate conjunction;

    @Mock
    private Path<String> nomePath;

    @Mock
    private Expression<String> lowerExpression;

    @Mock
    private Path<ProjetoStatus> statusPath;

    @Mock
    private Path<Object> gerentePath;

    @Mock private Path<Long> idPath;

    @Mock private Path<LocalDate> dataPath;


    @Test
    @DisplayName("Deve retornar predicado neutro quando nome for nulo ou branco")
    void deveRetornarPredicadoNeutroQuandoNomeForNuloOuBranco() {
        when(builder.conjunction()).thenReturn(conjunction);

        assertThat(ProjetoSpecifications.nomeContem(null).toPredicate(root, query, builder)).isSameAs(conjunction);
        assertThat(ProjetoSpecifications.nomeContem("  ").toPredicate(root, query, builder)).isSameAs(conjunction);
    }

    @Test
    @DisplayName("Deve aplicar filtro case insensitive por nome")
    void deveAplicarFiltroCaseInsensitivePorNome() {
        when(root.<String>get("nome")).thenReturn(nomePath);
        when(builder.lower(nomePath)).thenReturn(lowerExpression);
        when(builder.like(lowerExpression, "%erp%")).thenReturn(predicate);

        assertThat(ProjetoSpecifications.nomeContem("ERP").toPredicate(root, query, builder)).isSameAs(predicate);
    }

    @Test
    @DisplayName("Deve retornar predicado neutro quando status for nulo")
    void deveRetornarPredicadoNeutroQuandoStatusForNulo() {
        when(builder.conjunction()).thenReturn(conjunction);

        assertThat(ProjetoSpecifications.temStatus(null).toPredicate(root, query, builder)).isSameAs(conjunction);
    }

    @Test
    @DisplayName("Deve aplicar filtro por status")
    void deveAplicarFiltroPorStatus() {
        when(root.<ProjetoStatus>get("status")).thenReturn(statusPath);
        when(builder.equal(statusPath, ProjetoStatus.EM_ANALISE)).thenReturn(predicate);

        assertThat(ProjetoSpecifications.temStatus(ProjetoStatus.EM_ANALISE).toPredicate(root, query, builder))
                .isSameAs(predicate);
    }

    @Test
    @DisplayName("Deve retornar predicado neutro quando gerente for nulo")
    void deveRetornarPredicadoNeutroQuandoGerenteForNulo() {
        when(builder.conjunction()).thenReturn(conjunction);

        assertThat(ProjetoSpecifications.temGerente(null).toPredicate(root, query, builder)).isSameAs(conjunction);
    }

    @Test
    @DisplayName("Deve aplicar filtro por gerente")
    void deveAplicarFiltroPorGerente() {
        when(root.get("gerente")).thenReturn(gerentePath);
        when(gerentePath.<Long>get("id")).thenReturn(idPath);
        when(builder.equal(idPath, 10L)).thenReturn(predicate);

        assertThat(ProjetoSpecifications.temGerente(10L).toPredicate(root, query, builder)).isSameAs(predicate);
    }

    @Test
    @DisplayName("Deve aplicar ou ignorar filtro por data de inicio")
    void deveAplicarOuIgnorarFiltroPorDataDeInicio() {
        LocalDate data = LocalDate.of(2026, 1, 1);
        when(builder.conjunction()).thenReturn(conjunction);
        when(root.<LocalDate>get("dataInicio")).thenReturn(dataPath);
        when(builder.equal(dataPath, data)).thenReturn(predicate);

        assertThat(ProjetoSpecifications.iniciamEm(null).toPredicate(root, query, builder)).isSameAs(conjunction);
        assertThat(ProjetoSpecifications.iniciamEm(data).toPredicate(root, query, builder)).isSameAs(predicate);
        verify(root).get("dataInicio");
    }

    @Test
    @DisplayName("Deve aplicar ou ignorar filtro por data prevista de fim")
    void deveAplicarOuIgnorarFiltroPorDataPrevistaDeFim() {
        LocalDate data = LocalDate.of(2026, 7, 1);
        when(builder.conjunction()).thenReturn(conjunction);
        when(root.<LocalDate>get("dataPrevistaFim")).thenReturn(dataPath);
        when(builder.equal(dataPath, data)).thenReturn(predicate);

        assertThat(ProjetoSpecifications.encerramPreviamenteEm(null).toPredicate(root, query, builder))
                .isSameAs(conjunction);
        assertThat(ProjetoSpecifications.encerramPreviamenteEm(data).toPredicate(root, query, builder))
                .isSameAs(predicate);
        verify(root).get("dataPrevistaFim");
    }
}
