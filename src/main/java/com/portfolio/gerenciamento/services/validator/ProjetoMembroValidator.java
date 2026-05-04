package com.portfolio.gerenciamento.services.validator;

import com.portfolio.gerenciamento.configs.properties.ProjetoProperties;
import com.portfolio.gerenciamento.entities.Membro;
import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.Atribuicao;
import com.portfolio.gerenciamento.enums.ProjetoStatus;
import com.portfolio.gerenciamento.exceptions.InvalidProjectMemberException;
import com.portfolio.gerenciamento.exceptions.MemberAllocationLimitException;
import com.portfolio.gerenciamento.repositories.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Componente responsável por validar regras de negócio na associação
 * e remoção de membros em projetos.
 * <p>
 * Centraliza validações como limites de membros, alocações ativas e
 * restrições de atribuição, garantindo consistência no domínio.
 * </p>
 *
 * <p>
 * Utiliza configurações de {@link ProjetoProperties} e lança exceções
 * específicas em caso de violação das regras.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ProjetoMembroValidator {

    private final ProjetoRepository projetoRepository;
    private final ProjetoProperties projetoProperties;

    /**
     * Valida se um membro pode ser associado a um projeto.
     * Verifica se o membro é funcionário, se já está associado ao projeto,
     * se o projeto atingiu o limite máximo de membros e se o membro
     * atingiu o limite máximo de alocações ativas.
     *
     * @param projeto o projeto ao qual o membro será associado
     * @param membro  o membro a ser associado ao projeto
     * @throws InvalidProjectMemberException  se o membro não for funcionário ou já estiver associado
     * @throws MemberAllocationLimitException   se o projeto ou o membro atingirem o limite de alocações
     */
    public void validarAssociacao(Projeto projeto, Membro membro) {
        validarFuncionario(membro.getAtribuicao());
        if (projeto.getMembros().contains(membro)) {
            throw new InvalidProjectMemberException("Membro já está associado a este projeto");
        }
        if (projeto.getMembros().size() >= projetoProperties.maxMembros()) {
            throw new MemberAllocationLimitException("O projeto não pode ter mais de 10 membros");
        }
        long alocacoesAtivas = projetoRepository.countByMembroIdAndStatusNotIn(membro.getId(), ProjetoStatus.STATUS_INATIVOS);
        if (alocacoesAtivas >= projetoProperties.maxAlocacoesAtivas()) {
            throw new MemberAllocationLimitException("O membro não pode ser alocado em mais de 3 projetos ativos");
        }
    }

    /**
     * Valida se um membro pode ser removido de um projeto.
     * Verifica se o membro está associado ao projeto e se o projeto
     * possui mais de um membro antes de permitir a remoção.
     *
     * @param projeto o projeto do qual o membro será removido
     * @param membro  o membro a ser removido do projeto
     * @throws InvalidProjectMemberException se o membro não estiver associado ao projeto
     *                                        ou se o projeto tiver apenas um membro
     */
    public void validarRemocao(Projeto projeto, Membro membro) {
        if (!projeto.getMembros().contains(membro)) {
            throw new InvalidProjectMemberException("Membro não está associado a este projeto");
        }
        if (projeto.getMembros().size() <= 1) {
            throw new InvalidProjectMemberException("O projeto deve manter pelo menos 1 membro");
        }
    }

    /**
     * Valida se o membro possui a atribuição de funcionário.
     * Apenas membros com a atribuição de funcionário podem ser associados aos projetos.
     *
     * @param atribuicao a atribuição a ser validada
     * @throws InvalidProjectMemberException se a atribuição não for informada (null) ou se não for do tipo FUNCIONARIO
     */
    public void validarFuncionario(Atribuicao atribuicao) {
        if (atribuicao == null) {
            throw new InvalidProjectMemberException("A atribuição é obrigatória");
        }

        if (atribuicao != Atribuicao.FUNCIONARIO) {
            throw new InvalidProjectMemberException(
                    "Apenas membros com a atribuição de funcionário podem ser associados a projetos"
            );
        }
    }
}