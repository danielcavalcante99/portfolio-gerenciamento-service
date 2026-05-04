package com.portfolio.gerenciamento.services.classificacaorisco;

import com.portfolio.gerenciamento.configs.properties.ProjetoProperties;
import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.ClassificacaoRisco;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public final class ClassificacaoAltoRiscoService implements RegraClassificacaoRisco {

    private final ProjetoProperties projetoProperties;

    @Override
    public boolean atende(Projeto projeto) {
        LocalDate limiteSeisMeses = projeto.getDataInicio().plusMonths(6);

        boolean duracaoExcede = projeto.getDataPrevistaFim().isAfter(limiteSeisMeses);
        boolean orcamentoExcede = projeto.getOrcamentoTotal()
                .compareTo(projetoProperties.limiteOrcamentoAlto()) > 0;

        return orcamentoExcede || duracaoExcede;
    }

    @Override
    public ClassificacaoRisco classificacao() {
        return ClassificacaoRisco.ALTO;
    }
}