package com.SistemSchool.config;

import com.SistemSchool.dto.UserDTO;
import com.SistemSchool.io.Perfil;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;

public final class FinancialOperationGuard {

    private static final String ATTR_LOGGED_USER = "loggedUser";

    private FinancialOperationGuard() {
    }

    public static void requireFinancial() {
        FacesContext context = FacesContext.getCurrentInstance();
        Object session = context == null ? null : context.getExternalContext().getSession(false);
        Object loggedUser = session instanceof HttpSession httpSession
                ? httpSession.getAttribute(ATTR_LOGGED_USER)
                : null;

        if (loggedUser instanceof UserDTO.UserResponseDTO user && user.getPerfil() == Perfil.FINANCIAL) {
            return;
        }

        if (context != null) {
            context.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Acesso negado",
                    "Apenas o usuário financeiro pode editar ou excluir registros financeiros."));
        }
        throw new SecurityException("Apenas o usuário financeiro pode editar ou excluir registros financeiros.");
    }
}
