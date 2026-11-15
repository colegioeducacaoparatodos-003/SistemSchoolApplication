package com.SistemSchool.exception;

public class EmailNotVerifiedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmailNotVerifiedException() {
        super("Email não confirmado");
    }
}
