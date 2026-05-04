package com.portfolio.gerenciamento.mappers;

import com.portfolio.gerenciamento.dtos.request.CriarMembroRequest;
import com.portfolio.gerenciamento.dtos.response.MembroResponse;
import com.portfolio.gerenciamento.entities.Membro;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MembroMapper {

    Membro toEntity(CriarMembroRequest request);

    MembroResponse toResponse(Membro member);
}
