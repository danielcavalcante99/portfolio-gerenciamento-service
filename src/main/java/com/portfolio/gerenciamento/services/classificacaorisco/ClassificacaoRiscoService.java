package com.portfolio.gerenciamento.services.classificacaorisco;

import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.ClassificacaoRisco;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço responsável pela classificação de risco de projetos.
 * <p>
 * A classificação é determinada com base em dois critérios:
 * o orçamento total do projeto e sua duração em meses.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ClassificacaoRiscoService {

    private final List<RegraClassificacaoRisco> regras;

    /**
     * Classifica o risco de um projeto com base no orçamento e na duração.
     * <p>
     * As regras de classificação são aplicadas na seguinte ordem de prioridade:
     * </p>
     * <ul>
     *   <li><b>ALTO:</b> orçamento acima de R$ 500.000,00 ou duração superior a 6 meses</li>
     *   <li><b>MÉDIO:</b> orçamento acima de R$ 100.000,00 ou duração superior a 3 meses</li>
     *   <li><b>BAIXO:</b> orçamento de até R$ 100.000,00 e duração de até 3 meses</li>
     * </ul>
     *
     * @param projeto projeto a ser classificado, contendo orçamento e datas de início e término
     * @return {@link ClassificacaoRisco} representando o nível de risco do projeto
     */
    public ClassificacaoRisco classificar(Projeto projeto) {
        return regras.stream()
                .filter(regra -> regra.atende(projeto))
                .map(RegraClassificacaoRisco::classificacao)
                .findFirst()
                .orElse(ClassificacaoRisco.BAIXO);
    }
}