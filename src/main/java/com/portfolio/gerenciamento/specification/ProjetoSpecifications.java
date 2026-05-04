package com.portfolio.gerenciamento.specification;

import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.ProjetoStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Classe utilitária responsável por fornecer {@link Specification}s dinâmicas
 * para a entidade {@link Projeto}.
 * <p>
 * Cada método encapsula um critério de filtragem opcional, permitindo a composição
 * flexível de consultas através da API de {@link Specification} do Spring Data JPA.
 * </p>
 *
 * <p>
 * Convenção adotada:
 * <ul>
 *     <li>Quando o parâmetro de filtro é nulo ou inválido, a {@link Specification}
 *     retorna {@code builder.conjunction()}, não impactando a query final.</li>
 *     <li>As comparações são feitas respeitando o tipo do campo e regras de negócio.</li>
 * </ul>
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjetoSpecifications {

    /**
     * Cria uma {@link Specification} para filtrar projetos cujo nome contenha
     * o texto informado, ignorando diferenças entre maiúsculas e minúsculas.
     * <p>
     * Caso o nome seja nulo ou em branco, nenhum filtro é aplicado.
     * </p>
     *
     * @param nome texto parcial para busca no nome do projeto
     * @return {@link Specification} para filtro por nome ou neutra se não aplicável
     */
    public static Specification<Projeto> nomeContem(String nome) {
        return (root, query, builder) -> nome == null || nome.isBlank()
                ? builder.conjunction()
                : builder.like(builder.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }

    /**
     * Cria uma {@link Specification} para filtrar projetos pelo status informado.
     * <p>
     * Caso o status seja nulo, nenhum filtro é aplicado.
     * </p>
     *
     * @param status status do projeto
     * @return {@link Specification} para filtro por status ou neutra se não aplicável
     */
    public static Specification<Projeto> temStatus(ProjetoStatus status) {
        return (root, query, builder) -> status == null
                ? builder.conjunction()
                : builder.equal(root.get("status"), status);
    }

    /**
     * Cria uma {@link Specification} para filtrar projetos pelo identificador
     * do gerente responsável.
     * <p>
     * Caso o identificador seja nulo, nenhum filtro é aplicado.
     * </p>
     *
     * @param gerenteId identificador do gerente
     * @return {@link Specification} para filtro por gerente ou neutra se não aplicável
     */
    public static Specification<Projeto> temGerente(Long gerenteId) {
        return (root, query, builder) -> gerenteId == null
                ? builder.conjunction()
                : builder.equal(root.get("gerente").get("id"), gerenteId);
    }

    /**
     * Cria uma {@link Specification} para filtrar projetos pela data de início.
     * <p>
     * Caso a data seja nula, nenhum filtro é aplicado.
     * </p>
     *
     * @param dataCriacao data de início do projeto
     * @return {@link Specification} para filtro por data de início ou neutra se não aplicável
     */
    public static Specification<Projeto> iniciamEm(LocalDate dataCriacao) {
        return (root, query, builder) -> dataCriacao == null
                ? builder.conjunction()
                : builder.equal(root.get("dataInicio"), dataCriacao);
    }

    /**
     * Cria uma {@link Specification} para filtrar projetos pela data prevista de encerramento.
     * <p>
     * Caso a data seja nula, nenhum filtro é aplicado.
     * </p>
     *
     * @param dataPrevistaFim data prevista de encerramento do projeto
     * @return {@link Specification} para filtro por data prevista de fim ou neutra se não aplicável
     */
    public static Specification<Projeto> encerramPreviamenteEm(LocalDate dataPrevistaFim) {
        return (root, query, builder) -> dataPrevistaFim == null
                ? builder.conjunction()
                : builder.equal(root.get("dataPrevistaFim"), dataPrevistaFim);
    }
}
