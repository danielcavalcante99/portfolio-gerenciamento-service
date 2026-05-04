package com.portfolio.gerenciamento.mappers;

import com.portfolio.gerenciamento.dtos.request.CriarProjetoRequest;
import com.portfolio.gerenciamento.dtos.response.ProjetoResponse;
import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.ClassificacaoRisco;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = MembroMapper.class)
public interface ProjetoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "gerente", ignore = true)
    @Mapping(target = "membros", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Projeto toEntity(CriarProjetoRequest request);

    @Mapping(target = "classificacaoRisco", expression = "java(classificacaoRisco)")
    ProjetoResponse toResponse(Projeto projeto, ClassificacaoRisco classificacaoRisco);
}
