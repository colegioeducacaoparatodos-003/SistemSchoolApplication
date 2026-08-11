package com.SistemSchool.modulo_secrtaria.io;

public enum EstadoPagamento {

    PENDENTE("Pendente"),

    PAGO("Pago"),

    PARCIAL("Pagamento Parcial"),

    VENCIDO("Vencido"),

    CANCELADO("Cancelado");

    private final String descricao;

    EstadoPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

}