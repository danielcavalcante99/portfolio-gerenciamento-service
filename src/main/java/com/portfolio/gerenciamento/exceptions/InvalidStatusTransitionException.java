package com.portfolio.gerenciamento.exceptions;

public class InvalidStatusTransitionException extends BusinessException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
