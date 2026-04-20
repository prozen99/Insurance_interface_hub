package com.insurancehub.interfacehub.application.execution;

public class ExecutionNotAllowedException extends RuntimeException {

    public ExecutionNotAllowedException(String message) {
        super(message);
    }
}
