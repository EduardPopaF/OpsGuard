package com.opsguard.common.exception;

public class IncidentModificationNotAllowedException
        extends RuntimeException {

    public IncidentModificationNotAllowedException(
            String message
    ) {
        super(message);
    }
}