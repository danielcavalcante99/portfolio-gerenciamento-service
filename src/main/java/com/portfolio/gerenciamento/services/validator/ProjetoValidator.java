package com.portfolio.gerenciamento.services.validator;

import com.portfolio.gerenciamento.entities.Membro;
import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.Atribuicao;
import com.portfolio.gerenciamento.enums.ProjetoStatus;
import com.portfolio.gerenciamento.exceptions.BusinessException;
import com.portfolio.gerenciamento.exceptions.InvalidProjectManagerException;
import com.portfolio.gerenciamento.exceptions.InvalidStatusTransitionException;
import com.portfolio.gerenciamento.exceptions.ProjectDeletionNotAllowedException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Componente responsavel por validar regras de negocio do ciclo de vida do projeto.
 * <p>
 * Centraliza validacoes de datas, gerente, transicoes de status e regras de exclusao,
 * garantindo consistencia no dominio.
 * </p>
 */
@Component
public class ProjetoValidator {

    /**
     * Valida as datas do projeto, garantindo que a data prevista de encerramento
     * e a data real de encerramento nao sejam anteriores a data de inicio.
     *
     * @param dataInicio data de inicio do projeto
     * @param dataPrevistaFim data prevista de encerramento
     * @param dataRealFim data real de encerramento
     * @throws BusinessException se alguma das datas de encerramento for anterior a data de inicio
     */
    public void validarDatas(LocalDate dataInicio, LocalDate dataPrevistaFim, LocalDate dataRealFim) {
        if (dataPrevistaFim.isBefore(dataInicio)) {
            throw new BusinessException("A data prevista de encerramento n\u00e3o pode ser anterior \u00e0 data de in\u00edcio");
        }
        if (dataRealFim != null && dataRealFim.isBefore(dataInicio)) {
            throw new BusinessException("A data real de encerramento n\u00e3o pode ser anterior \u00e0 data de in\u00edcio");
        }
    }

    /**
     * Valida se o membro informado pode ser gerente de um projeto.
     * Apenas membros com a atribuicao {@link Atribuicao#GERENTE} podem ser definidos
     * como gerente responsavel em criacoes ou atualizacoes de projetos.
     *
     * @param gerente membro candidato a gerente do projeto
     * @throws InvalidProjectManagerException se o gerente for nulo ou nao possuir a atribuicao GERENTE
     */
    public void validarGerente(Membro gerente) {
        if (gerente == null || gerente.getAtribuicao() != Atribuicao.GERENTE) {
            throw new InvalidProjectManagerException(
                    "O gerente do projeto deve possuir a atribui\u00e7\u00e3o GERENTE"
            );
        }
    }

    /**
     * Valida se a transicao entre dois status do ciclo de vida do projeto e permitida.
     * Transicoes para o mesmo status sao ignoradas sem lancar excecao.
     *
     * @param statusAtual   o status atual do projeto
     * @param statusDestino o status para o qual se deseja transicionar
     * @throws InvalidStatusTransitionException se a transicao entre os status nao for permitida
     */
    public void validarTransicaoStatus(ProjetoStatus statusAtual, ProjetoStatus statusDestino) {
        if (statusAtual == statusDestino) {
            return;
        }
        if (!statusAtual.podeTransicionarPara(statusDestino)) {
            throw new InvalidStatusTransitionException(
                    "Transi\u00e7\u00e3o de status inv\u00e1lida de " + statusAtual + " para " + statusDestino);
        }
    }

    /**
     * Valida se o projeto pode ser excluido com base em seu status atual.
     * Projetos com status INICIADO, EM_ANDAMENTO ou ENCERRADO nao podem ser excluidos.
     *
     * @param status o status a ser validado para exclusao
     * @throws ProjectDeletionNotAllowedException se o projeto estiver em um status que nao permite exclusao
     */
    public void validarExclusao(ProjetoStatus status) {
        if (ProjetoStatus.STATUS_NAO_DELETAVEIS.contains(status)) {
            throw new ProjectDeletionNotAllowedException(
                    "O projeto n\u00e3o pode ser exclu\u00eddo com o status " + status.getDescricao());
        }
    }

    /**
     * Valida se é possível preencher a data real do fim do projeto.
     *
     * <p>Um projeto pode ter sua data real de fim preenchida quando:
     * <ul>
     *   <li>O projeto não é nulo</li>
     *   <li>O status do projeto é inativo, ou seja, {@code ENCERRADO} ou {@code CANCELADO}</li>
     * </ul>
     *
     * @param projeto o projeto a ser validado
     * @return {@code true} se o projeto não é nulo e seu status é {@code ENCERRADO} ou {@code CANCELADO};
     *         {@code false} caso contrário
     */
    public boolean validarSePodePreencherDataRealFim(Projeto projeto) {
        return Objects.nonNull(projeto) && projeto.getStatus().isInativo();
    }
}
