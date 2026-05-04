package com.portfolio.gerenciamento.units.services.validator;

import com.portfolio.gerenciamento.entities.Membro;
import com.portfolio.gerenciamento.entities.Projeto;
import com.portfolio.gerenciamento.enums.Atribuicao;
import com.portfolio.gerenciamento.enums.ProjetoStatus;
import com.portfolio.gerenciamento.exceptions.BusinessException;
import com.portfolio.gerenciamento.exceptions.InvalidProjectManagerException;
import com.portfolio.gerenciamento.exceptions.InvalidStatusTransitionException;
import com.portfolio.gerenciamento.exceptions.ProjectDeletionNotAllowedException;
import com.portfolio.gerenciamento.services.validator.ProjetoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjetoValidatorTest {

    private static final LocalDate DATA_INICIO = LocalDate.of(2026, 1, 2);
    private static final LocalDate DATA_PREVISTA_VALIDA = LocalDate.of(2026, 1, 3);
    private static final LocalDate DATA_ANTERIOR_AO_INICIO = LocalDate.of(2026, 1, 1);
    private static final String MENSAGEM_GERENTE_INVALIDO = "O gerente do projeto deve possuir a atribuição GERENTE";

    private ProjetoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ProjetoValidator();
    }

    @DisplayName("Deve aceitar datas quando a data prevista e a data real forem validas")
    @ParameterizedTest(name = "prevista={1}, real={2}")
    @CsvSource(nullValues = "NULL", value = {
            "2026-01-01, 2026-01-01, NULL",
            "2026-01-01, 2026-01-02, 2026-01-03"
    })
    void should_notThrowException_when_datesAreValid(String inicio, String prevista, String real) {
        LocalDate dataInicio = LocalDate.parse(inicio);
        LocalDate dataPrevistaFim = LocalDate.parse(prevista);
        LocalDate dataRealFim = real == null ? null : LocalDate.parse(real);

        assertDoesNotThrow(() -> validator.validarDatas(
                dataInicio,
                dataPrevistaFim,
                dataRealFim
        ));
    }

    @Test
    @DisplayName("Deve rejeitar datas quando a data prevista for anterior ao inicio")
    void should_throwBusinessException_when_expectedEndDateIsBeforeStartDate() {
        BusinessException exception = assertThrows(BusinessException.class, () -> validator.validarDatas(
                DATA_INICIO,
                DATA_ANTERIOR_AO_INICIO,
                null
        ));

        assertThat(exception.getMessage()).isNotBlank();
    }

    @Test
    @DisplayName("Deve rejeitar datas quando a data real for anterior ao inicio")
    void should_throwBusinessException_when_actualEndDateIsBeforeStartDate() {
        BusinessException exception = assertThrows(BusinessException.class, () -> validator.validarDatas(
                DATA_INICIO,
                DATA_PREVISTA_VALIDA,
                DATA_ANTERIOR_AO_INICIO
        ));

        assertThat(exception.getMessage()).isNotBlank();
    }

    @Test
    @DisplayName("Deve aceitar gerente quando atribuicao for gerente")
    void should_notThrowException_when_memberAssignmentIsManager() {
        Membro gerente = membro(Atribuicao.GERENTE);

        assertDoesNotThrow(() -> validator.validarGerente(gerente));
    }

    @Test
    @DisplayName("Deve rejeitar gerente quando atribuicao for diferente de gerente")
    void should_throwInvalidProjectManagerException_when_memberAssignmentIsNotManager() {
        Membro funcionario = membro(Atribuicao.FUNCIONARIO);

        InvalidProjectManagerException exception = assertThrows(
                InvalidProjectManagerException.class,
                () -> validator.validarGerente(funcionario)
        );

        assertThat(exception.getMessage()).isEqualTo(MENSAGEM_GERENTE_INVALIDO);
    }

    @Test
    @DisplayName("Deve rejeitar gerente quando membro for nulo")
    void should_throwInvalidProjectManagerException_when_memberIsNull() {
        InvalidProjectManagerException exception = assertThrows(
                InvalidProjectManagerException.class,
                () -> validator.validarGerente(null)
        );

        assertThat(exception.getMessage()).isEqualTo(MENSAGEM_GERENTE_INVALIDO);
    }

    @DisplayName("Deve aceitar transicao de status quando destino for permitido")
    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "EM_ANALISE, EM_ANALISE",
            "EM_ANALISE, ANALISE_REALIZADA",
            "EM_ANDAMENTO, CANCELADO"
    })
    void should_notThrowException_when_statusTransitionIsAllowed(ProjetoStatus statusAtual, ProjetoStatus statusDestino) {
        assertDoesNotThrow(() -> validator.validarTransicaoStatus(statusAtual, statusDestino));
    }

    @Test
    @DisplayName("Deve rejeitar transicao de status quando destino for invalido")
    void should_throwInvalidStatusTransitionException_when_statusTransitionIsInvalid() {
        InvalidStatusTransitionException exception = assertThrows(InvalidStatusTransitionException.class, () ->
                validator.validarTransicaoStatus(ProjetoStatus.EM_ANALISE, ProjetoStatus.INICIADO));

        assertThat(exception.getMessage()).isNotBlank();
    }

    @DisplayName("Deve aceitar exclusao quando status for deletavel")
    @ParameterizedTest(name = "{0}")
    @EnumSource(value = ProjetoStatus.class, names = {"EM_ANALISE", "ANALISE_REALIZADA", "ANALISE_APROVADA", "CANCELADO"})
    void should_notThrowException_when_statusAllowsDeletion(ProjetoStatus status) {
        assertDoesNotThrow(() -> validator.validarExclusao(status));
    }

    @DisplayName("Deve rejeitar exclusao quando status nao for deletavel")
    @ParameterizedTest(name = "{0}")
    @EnumSource(value = ProjetoStatus.class, names = {"INICIADO", "EM_ANDAMENTO", "ENCERRADO"})
    void should_throwProjectDeletionNotAllowedException_when_statusDoesNotAllowDeletion(ProjetoStatus status) {
        ProjectDeletionNotAllowedException exception = assertThrows(
                ProjectDeletionNotAllowedException.class,
                () -> validator.validarExclusao(status)
        );

        assertThat(exception.getMessage()).isNotBlank();
    }

    @DisplayName("Deve permitir preencher data real fim quando projeto for inativo")
    @ParameterizedTest(name = "{0}")
    @EnumSource(value = ProjetoStatus.class, names = {"ENCERRADO", "CANCELADO"})
    void should_returnTrue_when_projectIsInactive(ProjetoStatus status) {
        Projeto projeto = projeto(status);

        boolean podePreencherDataRealFim = validator.validarSePodePreencherDataRealFim(projeto);

        assertThat(podePreencherDataRealFim).isTrue();
    }

    @DisplayName("Deve rejeitar preencher data real fim quando status ainda for ativo")
    @ParameterizedTest(name = "{0}")
    @EnumSource(value = ProjetoStatus.class, names = {
            "EM_ANALISE",
            "ANALISE_REALIZADA",
            "ANALISE_APROVADA",
            "INICIADO",
            "EM_ANDAMENTO"
    })
    void should_returnFalse_when_projectStatusIsActive(ProjetoStatus status) {
        Projeto projeto = projeto(status);

        boolean podePreencherDataRealFim = validator.validarSePodePreencherDataRealFim(projeto);

        assertThat(podePreencherDataRealFim).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar preencher data real fim quando projeto for nulo")
    void should_returnFalse_when_projectIsNull() {
        boolean podePreencherDataRealFim = validator.validarSePodePreencherDataRealFim(null);

        assertThat(podePreencherDataRealFim).isFalse();
    }

    private static Membro membro(Atribuicao atribuicao) {
        return Membro.builder()
                .id(1L)
                .nome("Membro")
                .atribuicao(atribuicao)
                .build();
    }

    private static Projeto projeto(ProjetoStatus status) {
        return Projeto.builder()
                .status(status)
                .build();
    }
}
