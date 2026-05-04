package com.portfolio.gerenciamento.units.exceptions;

import com.portfolio.gerenciamento.dtos.response.ErrorResponse;
import com.portfolio.gerenciamento.exceptions.BusinessException;
import com.portfolio.gerenciamento.exceptions.ResourceNotFoundException;
import com.portfolio.gerenciamento.exceptions.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/recurso");

    @Test
    @DisplayName("Deve retornar 404 para recurso nao encontrado")
    void deveRetornar404ParaRecursoNaoEncontrado() {
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(
                new ResourceNotFoundException("Registro nao encontrado"),
                request
        );

        assertError(response, HttpStatus.NOT_FOUND, "Registro nao encontrado");
        assertNotNull(response.getBody());
        assertThat(response.getBody().campoErros()).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar 400 para erro de negocio")
    void deveRetornar400ParaErroDeNegocio() {
        ResponseEntity<ErrorResponse> response = handler.handleBusiness(
                new BusinessException("Regra violada"),
                request
        );

        assertError(response, HttpStatus.BAD_REQUEST, "Regra violada");
        assertNotNull(response.getBody());
        assertThat(response.getBody().campoErros()).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar erros por campo para falha de validacao")
    void deveRetornarErrosPorCampoParaFalhaDeValidacao() {
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("request", "nome", "nao deve estar em branco"),
                new FieldError("request", "status", "nao deve ser nulo")
        ));

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(exception, request);

        assertError(response, HttpStatus.BAD_REQUEST, "Falha na validação");
        assertNotNull(response.getBody());
        assertThat(response.getBody().campoErros())
                .containsEntry("nome", "nao deve estar em branco")
                .containsEntry("status", "nao deve ser nulo");
    }

    @Test
    @DisplayName("Deve retornar 500 para erro inesperado")
    void deveRetornar500ParaErroInesperado() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(
                new IllegalStateException("falha interna"),
                request
        );

        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado");
        assertNotNull(response.getBody());
        assertThat(response.getBody().campoErros()).isEmpty();
    }

    private static void assertError(ResponseEntity<ErrorResponse> response, HttpStatus status, String message) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().timestamp()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(status.value());
        assertThat(response.getBody().erro()).isEqualTo(status.getReasonPhrase());
        assertThat(response.getBody().mensagem()).isEqualTo(message);
        assertThat(response.getBody().caminho()).isEqualTo("/api/v1/recurso");
    }

}
