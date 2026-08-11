package com.SistemSchool.config;

import java.io.IOException;
import java.util.Arrays;

import com.SistemSchool.dto.UserDTO;
import com.SistemSchool.io.Perfil;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@RequestScoped
public class AccessControlBean {

    @Inject
    private SessionBean sessionBean;

    public void checkAccess(Perfil... allowedProfiles) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        UserDTO.UserResponseDTO loggedUser = sessionBean.getLoggedUser();

        // Não autenticado -> manda para login
        if (loggedUser == null) {
            redirect(externalContext, "/login.xhtml?faces-redirect=true");
            return;
        }

        // Autenticado mas sem permissão para este perfil -> manda para acesso negado
        boolean permitido = Arrays.asList(allowedProfiles).contains(loggedUser.getPerfil());
        if (!permitido) {
            redirect(externalContext, "/access-denied.xhtml?faces-redirect=true");
        }
    }

    private void redirect(ExternalContext externalContext, String url) {
        try {
            externalContext.redirect(externalContext.getRequestContextPath() + url);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao redirecionar", e);
        }
    }
}