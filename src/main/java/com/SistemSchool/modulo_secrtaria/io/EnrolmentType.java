package com.SistemSchool.modulo_secrtaria.io;

public enum EnrolmentType {

    ENROLMENT,
    CONFIRMATION;

    public boolean isENROLMENT(){
        return this == ENROLMENT;
    }

    public boolean isCONFIRMATION(){
        return this == CONFIRMATION;
    }
}
