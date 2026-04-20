package com.insurancehub.interfacehub.application;

public class DuplicateCodeException extends RuntimeException {

    public DuplicateCodeException(String message) {
        super(message);
    }
}
