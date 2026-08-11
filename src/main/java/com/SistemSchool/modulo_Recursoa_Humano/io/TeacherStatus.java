package com.SistemSchool.modulo_Recursoa_Humano.io;

public enum TeacherStatus {

    ACTIVE,
    ON_LEAVE,
    SUSPENDED,
    RESIGNED,
    RETIRED,
    TERMINATED,
    DECEASED;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isOnLeave() {
        return this == ON_LEAVE;
    }

    public boolean isSuspended() {
        return this == SUSPENDED;
    }

    public boolean isResigned() {
        return this == RESIGNED;
    }  

    public boolean isRetired() {
        return this == RETIRED;
    }

    public boolean isTerminated() {
        return this == TERMINATED;
    }

    public boolean isDeceased() {
        return this == DECEASED;
    }

}
