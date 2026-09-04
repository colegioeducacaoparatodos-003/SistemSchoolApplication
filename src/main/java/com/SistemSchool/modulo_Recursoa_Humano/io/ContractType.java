package com.SistemSchool.modulo_Recursoa_Humano.io;

public enum ContractType {

    EFETIVO("Efetivo"),
    TERMO_CERTO("Contrato a Termo Certo"),
    TERMO_INCERTO("Contrato a Termo Incerto"),
    PRESTACAO_SERVICOS("Prestação de Serviços"),
    ESTAGIO("Estágio");

    private final String descricao;

    ContractType(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isEFETIVO() {
        return this == EFETIVO;
    }

    public boolean isTERMO_CERTO() {
        return this == TERMO_CERTO;
    }

    public boolean isTERMO_INCERTO() {
        return this == TERMO_INCERTO;
    }

    public boolean isPRESTACAO_SERVICOS() {
        return this == PRESTACAO_SERVICOS;
    }

    public boolean isESTAGIO() {
        return this == ESTAGIO;
    }
}
