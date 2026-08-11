package com.SistemSchool.modulo_Financeiro.io;

public enum MovementType {

    INCOME("Entrada"),

    EXPENSE("Saída");

    private final String description;

    MovementType(String description) {

        this.description = description;

    }

    public String getDescription() {

        return description;

    }

}