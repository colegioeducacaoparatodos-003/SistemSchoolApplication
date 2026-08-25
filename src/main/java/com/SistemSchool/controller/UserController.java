package com.SistemSchool.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.SistemSchool.config.SessionBean;
import com.SistemSchool.dto.PersonDTO.PersonResponseDTO;
import com.SistemSchool.dto.UserDTO;
import com.SistemSchool.io.Perfil;
import com.SistemSchool.lazy.UserLazyDataModel;
import com.SistemSchool.service.UserService;
import com.SistemSchool.util.PerfilNotDefinedException;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class UserController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private static final String[] AVATAR_COLORS = {
            "#F44336", "#E91E63", "#9C27B0", "#673AB7",
            "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
            "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
            "#FFC107", "#FF9800", "#FF5722"
    };

    @Inject
    private transient UserService userService;

    @Inject
    private SessionBean sessionBean;

    // Dados para login
    private String loginEmail;
    private String loginPassword;
    private boolean rememberMe;
    private boolean loginDialogVisible;

    // Dados para novo usuário
    private String newFirstName;
    private String newMiddleName;
    private String newLastName;
    private String newUserEmail;
    private String newUserPassword;
    private String newUserConfirmPassword;
    private Integer newUserFkPerson = 0;
    private Perfil newUserPerfil;
    private boolean newUserActive = true;
    private String newUserDeviceToken;
    private boolean registerDialogVisible;
    private String firstName;
    private String lastName;
    private String imagePerson;

    // Dados para edição
    private UserDTO.UserResponseDTO selectedUser;
    private String editUserEmail;
    private String editUserDeviceToken;
    private boolean editUserActive;
    private Perfil editUserPerfil;
    private boolean editMode;

    // Listas e estado
    private List<UserDTO.UserResponseDTO> users = new ArrayList<>();
    private List<UserDTO.UserResponseDTO> filteredUsers;

    // Filtros
    private String filterEmail;
    private Perfil filterPerfil;
    private Boolean filterActive;

    // ========== RECUPERAÇÃO DE SENHA ==========
    private String recoveryEmail;
    private String recoveryPassword;
    private String recoveryConfirmPassword;
    private int recoveryStep = 1;
    private UserDTO.UserResponseDTO recoveryUser;

    // ========== LAZY MODEL ==========
    private UserLazyDataModel lazyUsers;

    @PostConstruct
    public void init() {
        logger.info("Inicializando UserController");
        this.lazyUsers = new UserLazyDataModel(userService);
        resetLoginFields();
        resetNewUserFields();
        resetEditFields();
    }

    // ========== VERIFICAÇÕES DE SEGURANÇA ==========

    public boolean isFirstAdminSetup() {
        try {
            return userService.countAdmins() == 0;
        } catch (Exception e) {
            logger.error("Erro ao verificar existência de administradores", e);
            return false;
        }
    }

    public boolean isAdminLoggedIn() {
        UserDTO.UserResponseDTO current = sessionBean.getLoggedUser();
        return current != null && current.getPerfil() == Perfil.ADMIN;
    }

    public List<Perfil> getAvailablePerfisForSignup() {
        if (isFirstAdminSetup()) {
            return List.of(Perfil.ADMIN);
        }
        if (isAdminLoggedIn()) {
            List<Perfil> lista = new ArrayList<>(List.of(Perfil.values()));
            lista.remove(Perfil.ADMIN);
            return lista;
        }
        return List.of();
    }

    // ========== AUTENTICAÇÃO ==========

    public String login() {
        try {
            logger.info("Tentativa de login com email: {}", loginEmail);

            if (!isLoginDataValid()) {
                return null;
            }

            UserDTO.LoginDTO loginDTO = new UserDTO.LoginDTO();
            loginDTO.setEmail(loginEmail.trim());
            loginDTO.setPassword(loginPassword);

            UserDTO.UserResponseDTO authenticatedUser = userService.authenticate(loginDTO);
            sessionBean.setLoggedUser(authenticatedUser);

            if (rememberMe) {
                logger.info("Lembrar-me ativado para usuário: {}", authenticatedUser.getEmail());
            }

            resetLoginFields();

            if (loginDialogVisible) {
                PrimeFaces.current().executeScript("PF('loginDialog').hide()");
                loginDialogVisible = false;
            }

            String redirectUrl = redirectByPerfil(authenticatedUser.getPerfil());
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Login realizado com sucesso!");
            return redirectUrl;

        } catch (PerfilNotDefinedException e) {
            logger.error("Usuário sem perfil definido tentou fazer login: {}", loginEmail, e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro de Acesso",
                    "Sua conta não possui um perfil de acesso definido. Contate o administrador.");
            sessionBean.clear();
            loginPassword = null;

        } catch (RuntimeException e) {
            logger.error("Falha no login para email: {}", loginEmail, e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro de Autenticação",
                    "Email ou senha inválidos. Por favor, tente novamente.");
            loginPassword = null;

        } catch (Exception e) {
            logger.error("Erro inesperado durante login", e);
            addMessage(FacesMessage.SEVERITY_FATAL, "Erro do Sistema",
                    "Ocorreu um erro inesperado. Por favor, contate o administrador.");
        }

        return null;
    }

    private String redirectByPerfil(Perfil perfil) {
        if (perfil == null) {
            throw new PerfilNotDefinedException("Usuário autenticado sem perfil definido: " + loginEmail);
        }
        return "/components/public/dashboard.xhtml?faces-redirect=true";
    }

    public void logout() {
        try {
            UserDTO.UserResponseDTO current = sessionBean.getLoggedUser();
            if (current != null) {
                logger.info("Usuário fazendo logout: {} (ID: {})", current.getEmail(), current.getPkUser());
            }

            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            sessionBean.clear();
            externalContext.invalidateSession();
            externalContext.redirect(externalContext.getRequestContextPath() + "/login.xhtml");

        } catch (IOException e) {
            logger.error("Erro ao fazer logout", e);
        }
    }

    public void ensureLoggedIn() {
        if (!sessionBean.isLoggedIn()) {
            try {
                ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
                externalContext.redirect(externalContext.getRequestContextPath() + "/login.xhtml");
            } catch (IOException e) {
                logger.error("Erro ao redirecionar usuário não autenticado", e);
            }
        }
    }

    private boolean isLoginDataValid() {
        if (loginEmail == null || loginEmail.trim().isEmpty()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Email é obrigatório");
            return false;
        }
        if (loginPassword == null || loginPassword.trim().isEmpty()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Senha é obrigatória");
            return false;
        }
        return true;
    }

    public void showLoginDialog() {
        resetLoginFields();
        loginDialogVisible = true;
        PrimeFaces.current().executeScript("PF('loginDialog').show()");
    }

    public boolean isUserLoggedIn() {
        return sessionBean.isLoggedIn();
    }

    // ========== HELPERS DE PERFIL ==========

    public Perfil[] getPerfis() {
        return Perfil.values();
    }

    public String getPerfilLabel(Perfil perfil) {
        if (perfil == null) {
            return "";
        }
        return switch (perfil) {
            case ADMIN -> "Administrador";
            case SECRETARY -> "Secretaria";
            case FINANCIAL -> "Financeiro";
            case PEDAGOGICAL -> "Pedagógico";
        };
    }

    // ========== CRUD DE USUÁRIOS ==========

    public String createUser() {
        try {
            logger.info("Criando novo usuário com email: {}", newUserEmail);

            if (!isNewUserDataValid()) {
                return null;
            }

            boolean firstSetup = isFirstAdminSetup();
            UserDTO.UserResponseDTO current = sessionBean.getLoggedUser();

            if (!firstSetup && (current == null || current.getPerfil() != Perfil.ADMIN)) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                        "Apenas o Administrador pode criar novas contas.");
                return null;
            }

            if (newUserPerfil == Perfil.ADMIN && !firstSetup) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                        "Já existe um Administrador no sistema. Apenas um é permitido.");
                return null;
            }

            if (firstSetup && newUserPerfil != Perfil.ADMIN) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                        "O primeiro utilizador deve ser um Administrador.");
                return null;
            }

            UserDTO.CreateUserDTO createUserDTO = new UserDTO.CreateUserDTO();
            createUserDTO.setEmail(newUserEmail.trim());
            createUserDTO.setPassword(newUserPassword);
            createUserDTO.setFkPerson(newUserFkPerson);
            createUserDTO.setPerfil(newUserPerfil);
            createUserDTO.setActive(newUserActive);
            createUserDTO.setDeviceToken(newUserDeviceToken);

            UserDTO.UserResponseDTO createdUser = userService.createUser(createUserDTO);
            users.add(createdUser);

            resetNewUserFields();
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Usuário criado com sucesso!");

            if (firstSetup) {
                return "/login.xhtml?faces-redirect=true";
            }
            return null;

        } catch (RuntimeException e) {
            logger.error("Erro ao criar usuário", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
        return null;
    }

    private boolean isNewUserDataValid() {
        if (newUserPassword == null || !newUserPassword.equals(newUserConfirmPassword)) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "As senhas não coincidem");
            return false;
        }
        if (newUserEmail == null || newUserEmail.trim().isEmpty()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Email é obrigatório");
            return false;
        }
        return true;
    }

    public void updateUser() {
        if (selectedUser == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Nenhum usuário selecionado");
            return;
        }

        try {
            logger.info("Atualizando usuário ID: {}", selectedUser.getPkUser());

            UserDTO.UpdateUserDTO updateUserDTO = new UserDTO.UpdateUserDTO();
            updateUserDTO.setPkUser(selectedUser.getPkUser());
            updateUserDTO.setEmail(editUserEmail);
            updateUserDTO.setPerfil(editUserPerfil);
            updateUserDTO.setActive(editUserActive);
            updateUserDTO.setDeviceToken(editUserDeviceToken);

            UserDTO.UserResponseDTO updatedUser = userService.updateUser(updateUserDTO);

            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getPkUser() == updatedUser.getPkUser()) {
                    users.set(i, updatedUser);
                    break;
                }
            }

            selectedUser = null;
            resetEditFields();
            PrimeFaces.current().executeScript("PF('editUserDialog').hide()");
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Usuário atualizado com sucesso!");

        } catch (RuntimeException e) {
            logger.error("Erro ao atualizar usuário", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void deleteUser() {
        if (selectedUser == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Nenhum usuário selecionado");
            return;
        }

        try {
            logger.info("Desativando usuário ID: {}", selectedUser.getPkUser());
            userService.updateUserStatus(selectedUser.getPkUser(), false);

            users.removeIf(user -> user.getPkUser() == selectedUser.getPkUser());
            selectedUser = null;

            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Usuário desativado com sucesso!");

        } catch (RuntimeException e) {
            logger.error("Erro ao desativar usuário", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void showRegisterDialog() {
        resetNewUserFields();
        registerDialogVisible = true;
        PrimeFaces.current().executeScript("PF('registerDialog').show()");
    }

    public String openSignUpPage() {
        resetNewUserFields();
        return "/components/dashboard/sign_in.xhtml?faces-redirect=true";
    }

    // ========== RECUPERAÇÃO DE SENHA ==========

    public String verifyEmailForRecovery() {
        if (recoveryEmail == null || recoveryEmail.trim().isEmpty()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Email é obrigatório");
            return null;
        }

        Optional<UserDTO.UserResponseDTO> userOpt = userService.getUserByEmail(recoveryEmail.trim());
        if (userOpt.isPresent()) {
            recoveryUser = userOpt.get();
            recoveryStep = 2;
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso",
                    "Conta encontrada. Defina a sua nova senha.");
        } else {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                    "Não existe conta associada a este email.");
        }
        return null;
    }

    public String recoverPassword() {
        if (recoveryUser == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                    "Sessão de recuperação inválida. Tente novamente.");
            recoveryStep = 1;
            return null;
        }

        if (recoveryPassword == null || recoveryPassword.trim().isEmpty()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Nova senha é obrigatória");
            return null;
        }

        if (recoveryPassword.length() < 6) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                    "A senha deve ter no mínimo 6 caracteres");
            return null;
        }

        if (!recoveryPassword.equals(recoveryConfirmPassword)) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "As senhas não coincidem");
            return null;
        }

        try {
            UserDTO.UpdateUserDTO updateDTO = new UserDTO.UpdateUserDTO();
            updateDTO.setPkUser(recoveryUser.getPkUser());
            updateDTO.setEmail(recoveryUser.getEmail());
            updateDTO.setPerfil(recoveryUser.getPerfil());
            updateDTO.setActive(recoveryUser.isActive());
            updateDTO.setDeviceToken(recoveryUser.getDeviceToken());
            updateDTO.setPassword(recoveryPassword);

            userService.updateUser(updateDTO);

            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso",
                    "Senha alterada com sucesso! Redirecionando para o login...");
            resetRecoveryFields();

            PrimeFaces.current().executeScript(
                    "setTimeout(function(){ window.location.href = '" +
                            FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() +
                            "/login.xhtml'; }, 1500);");

            return null;
        } catch (RuntimeException e) {
            logger.error("Erro ao recuperar senha para email: {}", recoveryEmail, e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                    "Não foi possível alterar a senha. Tente novamente.");
        }
        return null;
    }

    public void cancelRecovery() {
        resetRecoveryFields();
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/login.xhtml");
        } catch (IOException e) {
            logger.error("Erro ao redirecionar para login", e);
        }
    }

    private void resetRecoveryFields() {
        recoveryEmail = null;
        recoveryPassword = null;
        recoveryConfirmPassword = null;
        recoveryStep = 1;
        recoveryUser = null;
    }

    // ========== RESET DE CAMPOS ==========

    private void resetLoginFields() {
        loginEmail = null;
        loginPassword = null;
        rememberMe = false;
    }

    public void resetNewUserFields() {
        newFirstName = null;
        newMiddleName = null;
        newLastName = null;
        newUserEmail = null;
        newUserPassword = null;
        newUserConfirmPassword = null;
        newUserFkPerson = 0;
        newUserPerfil = null;
        newUserActive = true;
        newUserDeviceToken = null;
        imagePerson = "";
        firstName = "";
        lastName = "";
    }

    private void resetEditFields() {
        editUserEmail = null;
        editUserDeviceToken = null;
        editUserActive = true;
        editUserPerfil = null;
        editMode = false;
    }

    // ========== FILTROS (ATUALIZADOS PARA LAZY MODEL) ==========

    public void filterUsers() {
        if (lazyUsers != null) {
            lazyUsers.setFilterEmail(filterEmail);
            lazyUsers.setFilterPerfil(filterPerfil);
            lazyUsers.setFilterActive(filterActive);
        }
    }

    public void clearFilters() {
        filterEmail = null;
        filterPerfil = null;
        filterActive = null;
        if (lazyUsers != null) {
            lazyUsers.setFilterEmail(null);
            lazyUsers.setFilterPerfil(null);
            lazyUsers.setFilterActive(null);
        }
    }

    // ========== HELPERS DE APRESENTAÇÃO ==========

    public String getUserInitials() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isBlank()) {
            sb.append(firstName.substring(0, 1).toUpperCase());
        }
        if (lastName != null && !lastName.isBlank()) {
            sb.append(lastName.substring(0, 1).toUpperCase());
        }
        return sb.toString();
    }

    public String getInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return "?";
        }
        String[] parts = fullName.split(" ");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < Math.min(parts.length, 2); i++) {
            initials.append(parts[i].substring(0, 1).toUpperCase());
        }
        return initials.toString();
    }

    public String avatarClass(PersonResponseDTO person) {
        String name = person.getFullSearchName();
        if (name == null || name.isEmpty()) {
            name = "unknown";
        }
        int index = Math.abs(name.hashCode() % AVATAR_COLORS.length);
        return "avatar-color-" + index;
    }

    // ========== MENSAGENS ==========

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ========== GETTERS E SETTERS ==========

    public String getLoginEmail() {
        return loginEmail;
    }

    public void setLoginEmail(String loginEmail) {
        this.loginEmail = loginEmail;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public boolean isLoginDialogVisible() {
        return loginDialogVisible;
    }

    public void setLoginDialogVisible(boolean loginDialogVisible) {
        this.loginDialogVisible = loginDialogVisible;
    }

    public String getNewFirstName() {
        return newFirstName;
    }

    public void setNewFirstName(String newFirstName) {
        this.newFirstName = newFirstName;
    }

    public String getNewMiddleName() {
        return newMiddleName;
    }

    public void setNewMiddleName(String newMiddleName) {
        this.newMiddleName = newMiddleName;
    }

    public String getNewLastName() {
        return newLastName;
    }

    public void setNewLastName(String newLastName) {
        this.newLastName = newLastName;
    }

    public String getNewUserEmail() {
        return newUserEmail;
    }

    public void setNewUserEmail(String newUserEmail) {
        this.newUserEmail = newUserEmail;
    }

    public String getNewUserPassword() {
        return newUserPassword;
    }

    public void setNewUserPassword(String newUserPassword) {
        this.newUserPassword = newUserPassword;
    }

    public String getNewUserConfirmPassword() {
        return newUserConfirmPassword;
    }

    public void setNewUserConfirmPassword(String newUserConfirmPassword) {
        this.newUserConfirmPassword = newUserConfirmPassword;
    }

    public Integer getNewUserFkPerson() {
        return newUserFkPerson;
    }

    public void setNewUserFkPerson(Integer newUserFkPerson) {
        this.newUserFkPerson = newUserFkPerson;
    }

    public Perfil getNewUserPerfil() {
        return newUserPerfil;
    }

    public void setNewUserPerfil(Perfil newUserPerfil) {
        this.newUserPerfil = newUserPerfil;
    }

    public boolean isNewUserActive() {
        return newUserActive;
    }

    public void setNewUserActive(boolean newUserActive) {
        this.newUserActive = newUserActive;
    }

    public String getNewUserDeviceToken() {
        return newUserDeviceToken;
    }

    public void setNewUserDeviceToken(String newUserDeviceToken) {
        this.newUserDeviceToken = newUserDeviceToken;
    }

    public boolean isRegisterDialogVisible() {
        return registerDialogVisible;
    }

    public void setRegisterDialogVisible(boolean registerDialogVisible) {
        this.registerDialogVisible = registerDialogVisible;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getImagePerson() {
        return imagePerson;
    }

    public void setImagePerson(String imagePerson) {
        this.imagePerson = imagePerson;
    }

    public UserDTO.UserResponseDTO getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(UserDTO.UserResponseDTO selectedUser) {
        this.selectedUser = selectedUser;
    }

    public String getEditUserEmail() {
        return editUserEmail;
    }

    public void setEditUserEmail(String editUserEmail) {
        this.editUserEmail = editUserEmail;
    }

    public String getEditUserDeviceToken() {
        return editUserDeviceToken;
    }

    public void setEditUserDeviceToken(String editUserDeviceToken) {
        this.editUserDeviceToken = editUserDeviceToken;
    }

    public boolean isEditUserActive() {
        return editUserActive;
    }

    public void setEditUserActive(boolean editUserActive) {
        this.editUserActive = editUserActive;
    }

    public Perfil getEditUserPerfil() {
        return editUserPerfil;
    }

    public void setEditUserPerfil(Perfil editUserPerfil) {
        this.editUserPerfil = editUserPerfil;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public List<UserDTO.UserResponseDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserDTO.UserResponseDTO> users) {
        this.users = users;
    }

    public List<UserDTO.UserResponseDTO> getFilteredUsers() {
        return filteredUsers != null ? filteredUsers : users;
    }

    public void setFilteredUsers(List<UserDTO.UserResponseDTO> filteredUsers) {
        this.filteredUsers = filteredUsers;
    }

    public UserDTO.UserResponseDTO getLoggedUser() {
        return sessionBean.getLoggedUser();
    }

    public String getFilterEmail() {
        return filterEmail;
    }

    public void setFilterEmail(String filterEmail) {
        this.filterEmail = filterEmail;
    }

    public Perfil getFilterPerfil() {
        return filterPerfil;
    }

    public void setFilterPerfil(Perfil filterPerfil) {
        this.filterPerfil = filterPerfil;
    }

    public Boolean getFilterActive() {
        return filterActive;
    }

    public void setFilterActive(Boolean filterActive) {
        this.filterActive = filterActive;
    }

    public String getRecoveryEmail() {
        return recoveryEmail;
    }

    public void setRecoveryEmail(String recoveryEmail) {
        this.recoveryEmail = recoveryEmail;
    }

    public String getRecoveryPassword() {
        return recoveryPassword;
    }

    public void setRecoveryPassword(String recoveryPassword) {
        this.recoveryPassword = recoveryPassword;
    }

    public String getRecoveryConfirmPassword() {
        return recoveryConfirmPassword;
    }

    public void setRecoveryConfirmPassword(String recoveryConfirmPassword) {
        this.recoveryConfirmPassword = recoveryConfirmPassword;
    }

    public int getRecoveryStep() {
        return recoveryStep;
    }

    public void setRecoveryStep(int recoveryStep) {
        this.recoveryStep = recoveryStep;
    }

    public UserDTO.UserResponseDTO getRecoveryUser() {
        return recoveryUser;
    }

    public void setRecoveryUser(UserDTO.UserResponseDTO recoveryUser) {
        this.recoveryUser = recoveryUser;
    }

    // ========== LAZY MODEL GETTER ==========

    public UserLazyDataModel getLazyUsers() {
        return lazyUsers;
    }
}