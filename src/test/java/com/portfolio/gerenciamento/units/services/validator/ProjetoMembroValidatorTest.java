package com.portfolio.gerenciamento.units.services.validator;

import com.portfolio.gerenciamento.configs.properties.ProjetoProperties;
import com.portfolio.gerenciamento.entities.Membro;
import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.Atribuicao;
import com.portfolio.gerenciamento.enums.ProjetoStatus;
import com.portfolio.gerenciamento.exceptions.InvalidProjectMemberException;
import com.portfolio.gerenciamento.exceptions.MemberAllocationLimitException;
import com.portfolio.gerenciamento.repositories.ProjetoRepository;
import com.portfolio.gerenciamento.services.validator.ProjetoMembroValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetoMembroValidatorTest {

    private static final int LIMITE_MAXIMO_MEMBROS = 10;
    private static final int LIMITE_MAXIMO_ALOCACOES_ATIVAS = 3;
    private static final BigDecimal LIMITE_ORCAMENTO_MEDIO_RISCO = BigDecimal.valueOf(100_000);
    private static final BigDecimal LIMITE_ORCAMENTO_ALTO_RISCO = BigDecimal.valueOf(500_000);
    private static final long MEMBRO_ID = 1L;
    private static final long OUTRO_MEMBRO_ID = 2L;

    @Mock
    private ProjetoRepository projetoRepository;

    private ProjetoMembroValidator validator;

    @BeforeEach
    void setUp() {
        validator = validatorComLimites(LIMITE_MAXIMO_MEMBROS, LIMITE_MAXIMO_ALOCACOES_ATIVAS);
    }

    @Test
    @DisplayName("Deve aceitar associacao quando membro for funcionario e limites nao forem estourados")
    void should_notThrowException_when_memberIsEmployeeAndAllocationLimitsAreAvailable() {
        Projeto projeto = projeto(Set.of());
        Membro membro = membro(MEMBRO_ID, Atribuicao.FUNCIONARIO);

        when(projetoRepository.countByMembroIdAndStatusNotIn(MEMBRO_ID, ProjetoStatus.STATUS_INATIVOS)).thenReturn(2L);

        assertThatCode(() -> validator.validarAssociacao(projeto, membro)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve rejeitar associacao quando atribuicao for nula")
    void should_throwInvalidProjectMemberException_when_memberAssignmentIsNull() {
        Projeto projeto = projeto(Set.of());
        Membro membro = membro(MEMBRO_ID, null);

        assertThrows(InvalidProjectMemberException.class, () -> validator.validarAssociacao(projeto, membro));
    }

    @Test
    @DisplayName("Deve rejeitar associacao quando membro nao for funcionario")
    void should_throwInvalidProjectMemberException_when_memberIsNotEmployee() {
        Projeto projeto = projeto(Set.of());
        Membro membro = membro(MEMBRO_ID, Atribuicao.GERENTE);

        assertThrows(InvalidProjectMemberException.class, () -> validator.validarAssociacao(projeto, membro));
    }

    @Test
    @DisplayName("Deve rejeitar associacao quando membro ja estiver associado")
    void should_throwInvalidProjectMemberException_when_memberAlreadyBelongsToProject() {
        Membro membro = membro(MEMBRO_ID, Atribuicao.FUNCIONARIO);
        Projeto projeto = projeto(Set.of(membro));

        assertThrows(InvalidProjectMemberException.class, () -> validator.validarAssociacao(projeto, membro));
    }

    @Test
    @DisplayName("Deve rejeitar associacao quando projeto atingir limite maximo de membros")
    void should_throwMemberAllocationLimitException_when_projectHasMaximumMembers() {
        ProjetoMembroValidator validatorComLimiteDeUmMembro = validatorComLimites(1, LIMITE_MAXIMO_ALOCACOES_ATIVAS);
        Membro membroAssociado = membro(MEMBRO_ID, Atribuicao.FUNCIONARIO);
        Membro novoMembro = membro(OUTRO_MEMBRO_ID, Atribuicao.FUNCIONARIO);
        Projeto projeto = projeto(Set.of(membroAssociado));

        assertThrows(
                MemberAllocationLimitException.class,
                () -> validatorComLimiteDeUmMembro.validarAssociacao(projeto, novoMembro)
        );
    }

    @Test
    @DisplayName("Deve rejeitar associacao quando membro atingir limite maximo de alocacoes ativas")
    void should_throwMemberAllocationLimitException_when_memberHasMaximumActiveAllocations() {
        Membro membro = membro(MEMBRO_ID, Atribuicao.FUNCIONARIO);
        Projeto projeto = projeto(Set.of());

        when(projetoRepository.countByMembroIdAndStatusNotIn(MEMBRO_ID, ProjetoStatus.STATUS_INATIVOS))
                .thenReturn((long) LIMITE_MAXIMO_ALOCACOES_ATIVAS);

        assertThrows(MemberAllocationLimitException.class, () -> validator.validarAssociacao(projeto, membro));
    }

    @Test
    @DisplayName("Deve aceitar remocao quando membro estiver associado e projeto mantiver outro membro")
    void should_notThrowException_when_memberIsAssociatedAndProjectKeepsAnotherMember() {
        Membro membro = membro(MEMBRO_ID, Atribuicao.FUNCIONARIO);
        Membro outroMembro = membro(OUTRO_MEMBRO_ID, Atribuicao.FUNCIONARIO);
        Projeto projeto = projeto(Set.of(membro, outroMembro));

        assertThatCode(() -> validator.validarRemocao(projeto, membro)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve rejeitar remocao quando membro nao estiver associado")
    void should_throwInvalidProjectMemberException_when_memberIsNotAssociated() {
        Membro membro = membro(MEMBRO_ID, Atribuicao.FUNCIONARIO);
        Projeto projeto = projeto(Set.of());

        assertThrows(InvalidProjectMemberException.class, () -> validator.validarRemocao(projeto, membro));
    }

    @Test
    @DisplayName("Deve rejeitar remocao quando projeto ficaria sem membros")
    void should_throwInvalidProjectMemberException_when_projectWouldHaveNoMembers() {
        Membro membro = membro(MEMBRO_ID, Atribuicao.FUNCIONARIO);
        Projeto projeto = projeto(Set.of(membro));

        assertThrows(InvalidProjectMemberException.class, () -> validator.validarRemocao(projeto, membro));
    }

    @Test
    @DisplayName("Deve aceitar funcionario quando atribuicao for funcionario")
    void should_notThrowException_when_assignmentIsEmployee() {
        assertThatCode(() -> validator.validarFuncionario(Atribuicao.FUNCIONARIO)).doesNotThrowAnyException();
    }

    private ProjetoMembroValidator validatorComLimites(int maxMembros, int maxAlocacoesAtivas) {
        return new ProjetoMembroValidator(
                projetoRepository,
                new ProjetoProperties(
                        maxMembros,
                        maxAlocacoesAtivas,
                        LIMITE_ORCAMENTO_MEDIO_RISCO,
                        LIMITE_ORCAMENTO_ALTO_RISCO
                )
        );
    }

    private static Projeto projeto(Set<Membro> membros) {
        return Projeto.builder()
                .id(1L)
                .status(ProjetoStatus.EM_ANALISE)
                .membros(new HashSet<>(membros))
                .build();
    }

    private static Membro membro(Long id, Atribuicao atribuicao) {
        return Membro.builder()
                .id(id)
                .nome("Membro " + id)
                .atribuicao(atribuicao)
                .build();
    }
}
