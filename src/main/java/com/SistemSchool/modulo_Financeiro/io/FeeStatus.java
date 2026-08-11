package com.SistemSchool.modulo_Financeiro.io;

public enum FeeStatus {

    ACTIVE("feeStatus.active"),
    INACTIVE("feeStatus.inactive");

    private final String key;

    FeeStatus(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}