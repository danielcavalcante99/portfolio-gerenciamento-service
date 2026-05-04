package com.portfolio.gerenciamento.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum ProjetoStatus {

    EM_ANALISE("Em Análise"),
    ANALISE_REALIZADA("Análise Realizada"),
    ANALISE_APROVADA("Análise Aprovada"),
    INICIADO("Iniciado"),
    EM_ANDAMENTO("Em Andamento"),
    ENCERRADO("Encerrado"),
    CANCELADO("Cancelado");

    private final String descricao;

    public static final List<ProjetoStatus> STATUS_INATIVOS =
            List.of(ENCERRADO, CANCELADO);

    public static final Set<ProjetoStatus> STATUS_NAO_DELETAVEIS = EnumSet.of(
            ProjetoStatus.INICIADO,
            ProjetoStatus.EM_ANDAMENTO,
            ProjetoStatus.ENCERRADO
    );

    public ProjetoStatus proximo() {
        return switch (this) {
            case EM_ANALISE       -> ANALISE_REALIZADA;
            case ANALISE_REALIZADA -> ANALISE_APROVADA;
            case ANALISE_APROVADA  -> INICIADO;
            case INICIADO          -> EM_ANDAMENTO;
            case EM_ANDAMENTO      -> ENCERRADO;
            default                -> null; // estados finais: ENCERRADO e CANCELADO
        };
    }

    public boolean podeTransicionarPara(ProjetoStatus status) {
        return status == CANCELADO || proximo() == status;
    }

    public boolean isInativo() {
        return ProjetoStatus.STATUS_INATIVOS.contains(this);
    }


}
