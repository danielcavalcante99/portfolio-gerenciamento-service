package com.portfolio.gerenciamento.repositories;

import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.ProjetoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;

public interface ProjetoRepository extends JpaRepository<Projeto, Long>, JpaSpecificationExecutor<Projeto> {

    /**
     * Conta a quantidade de projetos aos quais um membro está associado,
     * desconsiderando projetos que possuam status presentes na coleção informada.
     *
     * @param membroId identificador do membro
     * @param statuses coleção de status que devem ser ignorados na contagem
     * @return quantidade de projetos ativos (ou não excluídos pelos status informados)
     */
    @Query("""
            select count(projeto)
            from Projeto projeto
            join projeto.membros membro
            where membro.id = :membroId
              and projeto.status not in :statuses
            """)
    long countByMembroIdAndStatusNotIn(Long membroId, Collection<ProjetoStatus> statuses);

    /**
     * Conta o total de membros distintos que estão alocados em pelo menos um projeto.
     *
     * @return quantidade de membros únicos com alocação em projetos
     */
    @Query("select count(distinct membro.id) from Projeto projeto join projeto.membros membro")
    long countDistinctMembrosAlocados();

}
