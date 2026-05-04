package com.portfolio.gerenciamento.exceptions;

public class ProjectDeletionNotAllowedException extends BusinessException {

    public ProjectDeletionNotAllowedException(String message) {
        super(message);
    }
}
