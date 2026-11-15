package com.SistemSchool.report;

import com.SistemSchool.config.SessionBean;
import com.SistemSchool.dto.UserDTO.UserResponseDTO;

public final class PdfAuditIdentity {

    private PdfAuditIdentity() {}

    public static String getCurrentUserLabel() {
        UserResponseDTO user = SessionBean.getLoggedUserForCurrentRequest();
        if (user == null) {
            throw new IllegalStateException("Não é possível gerar um PDF sem um utilizador autenticado.");
        }

        String personName = user.getPersonName();
        String identity = personName == null || personName.isBlank()
                ? "Utilizador (ID " + user.getPkUser() + ")"
                : personName.trim() + " (ID " + user.getPkUser() + ")";
        return "Emitido por: " + identity;
    }
}
