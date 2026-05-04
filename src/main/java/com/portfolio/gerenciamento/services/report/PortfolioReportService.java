package com.portfolio.gerenciamento.services.report;

import com.portfolio.gerenciamento.dtos.response.ResumoPorfolioResponse;
import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.ProjetoStatus;
import com.portfolio.gerenciamento.repositories.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serviço responsável pela geração de relatórios e resumos do portfólio de projetos.
 */
@Service
@RequiredArgsConstructor
public class PortfolioReportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioReportService.class);

    private final ProjetoRepository projetoRepository;

    /**
     * Gera um resumo consolidado do portfólio de projetos.
     * <p>
     * O resumo inclui a contagem de projetos por status, o orçamento total agrupado
     * por status, a duração média dos projetos encerrados e o total de membros alocados.
     * </p>
     *
     * @return {@link ResumoPorfolioResponse} com os dados consolidados do portfólio
     */
    @Transactional(readOnly = true)
    public ResumoPorfolioResponse getResumo() {
        List<Projeto> projetos = projetoRepository.findAll();
        LOGGER.info("Resumo do portfólio gerado");
        return new ResumoPorfolioResponse(
                contarPorStatus(projetos),
                orcamentoPorStatus(projetos),
                duracaoMediaEncerrados(projetos),
                projetoRepository.countDistinctMembrosAlocados()
        );
    }

    /**
     * Contabiliza a quantidade de projetos agrupados por status.
     *
     * @param projetos lista de projetos a ser processada
     * @return mapa contendo a contagem de projetos para cada {@link ProjetoStatus}
     */
    private Map<ProjetoStatus, Long> contarPorStatus(List<Projeto> projetos) {
        return projetos.stream()
                .collect(Collectors.groupingBy(
                        Projeto::getStatus,
                        () -> new EnumMap<>(ProjetoStatus.class),
                        Collectors.counting()
                ));
    }

    /**
     * Calcula o orçamento total dos projetos agrupados por status.
     *
     * @param projetos lista de projetos a ser processada
     * @return mapa contendo a soma dos orçamentos para cada {@link ProjetoStatus}
     */
    private Map<ProjetoStatus, BigDecimal> orcamentoPorStatus(List<Projeto> projetos) {
        return projetos.stream()
                .collect(Collectors.groupingBy(
                        Projeto::getStatus,
                        () -> new EnumMap<>(ProjetoStatus.class),
                        Collectors.reducing(BigDecimal.ZERO, Projeto::getOrcamentoTotal, BigDecimal::add)
                ));
    }

    /**
     * Calcula a duração média em dias dos projetos com status {@code ENCERRADO}.
     * <p>
     * Apenas projetos que possuem data de término real preenchida são considerados no cálculo.
     * Caso nenhum projeto encerrado seja encontrado, retorna {@code 0.0}.
     * </p>
     *
     * @param projetos lista de projetos a ser processada
     * @return média de dias de duração dos projetos encerrados, ou {@code 0.0} se não houver nenhum
     */
    private Double duracaoMediaEncerrados(List<Projeto> projetos) {
        return projetos.stream()
                .filter(projeto -> projeto.getStatus() == ProjetoStatus.ENCERRADO)
                .filter(projeto -> projeto.getDataRealFim() != null)
                .mapToLong(projeto -> ChronoUnit.DAYS.between(projeto.getDataInicio(), projeto.getDataRealFim()))
                .average()
                .orElse(0.0);
    }
}