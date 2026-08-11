package com.SistemSchool.config;

import java.io.Serializable;

import com.SistemSchool.dto.UserDTO;
import com.SistemSchool.io.Perfil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IMPORTANTE: esta classe passou a ser @ApplicationScoped (singleton) DE
 * PROPÓSITO. Isto não é um erro — é a correção.
 *
 * Anteriormente esta classe era @SessionScoped e guardava o utilizador
 * logado num campo de instância. Isso obrigava a confiar que o CDI
 * (nesta stack Spring Boot + JoinFaces + MyFaces) resolvia sempre a
 * instância correta por sessão HTTP — o que se mostrou não ser fiável
 * e causava a mistura de contas entre dispositivos diferentes.
 *
 * Agora este bean não guarda NENHUM estado de utilizador em campos de
 * instância. Ele é apenas uma fachada (facade) que lê e escreve
 * diretamente na HttpSession real de cada pedido, através do
 * ExternalContext do JSF. Como cada dispositivo/browser tem o seu
 * próprio JSESSIONID e portanto a sua própria HttpSession no servidor,
 * isto garante isolamento correto entre utilizadores, independentemente
 * de qualquer ambiguidade de scope do CDI.
 */
@Named
@ApplicationScoped
public class SessionBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SessionBean.class);
    private static final String ATTR_LOGGED_USER = "loggedUser";

    public UserDTO.UserResponseDTO getLoggedUser() {
        ExternalContext externalContext = currentExternalContext();
        if (externalContext == null) {
            return null;
        }
        Object session = externalContext.getSession(false);
        if (session == null) {
            return null;
        }
        return (UserDTO.UserResponseDTO) ((HttpSession) session).getAttribute(ATTR_LOGGED_USER);
    }

    public void setLoggedUser(UserDTO.UserResponseDTO loggedUser) {
        ExternalContext externalContext = currentExternalContext();
        if (externalContext == null) {
            throw new IllegalStateException("Não é possível definir o utilizador logado fora de um pedido JSF");
        }
        HttpSession session = (HttpSession) externalContext.getSession(true);
        session.setAttribute(ATTR_LOGGED_USER, loggedUser);

        logger.info("Login associado à sessão HTTP [{}] -> utilizador: {} (perfil: {})",
                session.getId(),
                loggedUser != null ? loggedUser.getEmail() : "null",
                loggedUser != null ? loggedUser.getPerfil() : "null");
    }

    public void clear() {
        ExternalContext externalContext = currentExternalContext();
        if (externalContext == null) {
            return;
        }
        Object session = externalContext.getSession(false);
        if (session != null) {
            logger.info("Logout na sessão HTTP [{}]", ((HttpSession) session).getId());
            ((HttpSession) session).removeAttribute(ATTR_LOGGED_USER);
        }
    }

    public boolean isLoggedIn() {
        return getLoggedUser() != null;
    }

    public boolean isAdmin() {
        return hasPerfil(Perfil.ADMIN);
    }

    public boolean isSecretary() {
        return hasPerfil(Perfil.SECRETARY);
    }

    public boolean isFinancial() {
        return hasPerfil(Perfil.FINANCIAL);
    }

    public boolean isPedagogical() {
        return hasPerfil(Perfil.PEDAGOGICAL);
    }

    private boolean hasPerfil(Perfil perfil) {
        UserDTO.UserResponseDTO loggedUser = getLoggedUser();
        return loggedUser != null && loggedUser.getPerfil() == perfil;
    }

    private ExternalContext currentExternalContext() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        return facesContext != null ? facesContext.getExternalContext() : null;
    }
}