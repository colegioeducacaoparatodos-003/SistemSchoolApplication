package com.SistemSchool.modulo_secrtaria.io;

public enum ShiftType {

    MORNING("Manhã"),

    AFTERNOON("Tarde"),

    EVENING("Noite"),

    FULL_TIME("Tempo Integral");

    private final String descricao;

    ShiftType(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

}