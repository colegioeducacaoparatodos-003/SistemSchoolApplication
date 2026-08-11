package com.SistemSchool.modulo_secrtaria.io;

public enum FormaPagamento {

    DINHEIRO("Dinheiro"),

    MULTICAIXA("Multicaixa"),

    TRANSFERENCIA("Transferência Bancária"),

    PIX("PIX"),

    CHEQUE("Cheque");

    private final String descricao;

    FormaPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

}