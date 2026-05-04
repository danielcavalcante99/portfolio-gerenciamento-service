package com.portfolio.gerenciamento.services.classificacaorisco;

import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.ClassificacaoRisco;

public sealed interface RegraClassificacaoRisco permits ClassificacaoAltoRiscoService, ClassificacaoMedioRiscoService {

    boolean atende(Projeto projeto);

    ClassificacaoRisco classificacao();
}