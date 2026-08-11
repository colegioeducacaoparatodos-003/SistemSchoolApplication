package com.SistemSchool.modulo_Recursoa_Humano.io;

public enum ContractType {

    PERMANENT,
    FIXED_TERM,
    PART_TIME,
    FREELANCE,
    INTERN;

    public boolean isPermanent() {
        return this == PERMANENT;
    }

    public boolean isFixedTerm() {
        return this == FIXED_TERM;
    }

    public boolean isPartTime() {
        return this == PART_TIME;
    }

    public boolean isFreelance() {
        return this == FREELANCE;
    }

    public boolean isIntern() {
        return this == INTERN;
    }

}