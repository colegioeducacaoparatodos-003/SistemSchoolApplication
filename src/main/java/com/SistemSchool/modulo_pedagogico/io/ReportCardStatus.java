package com.SistemSchool.modulo_pedagogico.io;

public enum ReportCardStatus {

    DRAFT("Rascunho"),
    ISSUED("Emitido");

    private final String description;

    ReportCardStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}