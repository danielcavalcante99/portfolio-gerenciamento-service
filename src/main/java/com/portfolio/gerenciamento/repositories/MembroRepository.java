package com.portfolio.gerenciamento.repositories;

import com.portfolio.gerenciamento.entities.Membro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembroRepository extends JpaRepository<Membro, Long> {
}
