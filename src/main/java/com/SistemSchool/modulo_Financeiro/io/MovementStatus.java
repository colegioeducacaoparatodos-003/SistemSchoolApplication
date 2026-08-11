package com.SistemSchool.modulo_Financeiro.io;

public enum MovementStatus {

    ACTIVE("Ativo"),

    INACTIVE("Inativo"),

    PENDING("Pendente"),

    CANCELLED("Cancelado");

    private final String description;

    MovementStatus(String description) {

        this.description = description;

    }

    public String getDescription() {

        return description;

    }

}