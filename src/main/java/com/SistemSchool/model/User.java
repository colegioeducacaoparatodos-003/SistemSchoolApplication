package com.SistemSchool.model;

import java.time.LocalDateTime;

import com.SistemSchool.io.Perfil;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int pkUser;

    private int fkPerson;

    @Enumerated(EnumType.STRING)
    private Perfil perfil;

    private int fkCustomer;

    private String password;

    private String email;

    private boolean active;

    private String salt; // mantido só se ainda usares noutro sítio; com BCrypt deixa de ser necessário
    private String deviceToken;
    private Boolean emailVerified;
    @Column(unique = true)
    private String emailVerificationToken;
    private LocalDateTime emailVerificationTokenExpiry;
    @Column(unique = true)
    private String resetToken;
    private LocalDateTime resetTokenExpiry;
    private LocalDateTime userCreationDate;
    private LocalDateTime userModificationDate;

    public User() {
        super();
    }

    public int getPkUser() { return pkUser; }
    public void setPkUser(int pkUser) { this.pkUser = pkUser; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public int getFkCustomer() { return fkCustomer; }
    public void setFkCustomer(int fkCustomer) { this.fkCustomer = fkCustomer; }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

    @PrePersist
    protected void onCreate() {
        this.userCreationDate = LocalDateTime.now();
        this.userModificationDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.userModificationDate = LocalDateTime.now();
    }

    public LocalDateTime getUserCreationDate() { return userCreationDate; }
    public void setUserCreationDate(LocalDateTime userCreationDate) { this.userCreationDate = userCreationDate; }

    public LocalDateTime getUserModificationDate() { return userModificationDate; }
    public void setUserModificationDate(LocalDateTime userModificationDate) { this.userModificationDate = userModificationDate; }

    public int getFkPerson() { return fkPerson; }
    public void setFkPerson(int fkPerson) { this.fkPerson = fkPerson; }

    public String getDeviceToken() { return deviceToken; }
    public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getEmailVerificationToken() { return emailVerificationToken; }
    public void setEmailVerificationToken(String emailVerificationToken) { this.emailVerificationToken = emailVerificationToken; }

    public LocalDateTime getEmailVerificationTokenExpiry() { return emailVerificationTokenExpiry; }
    public void setEmailVerificationTokenExpiry(LocalDateTime emailVerificationTokenExpiry) {
        this.emailVerificationTokenExpiry = emailVerificationTokenExpiry;
    }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }

    @Override
    public String toString() {
        return "User [pkUser=" + pkUser + ", fkPerson=" + fkPerson + ", perfil=" + perfil + ", email=" + email
                + ", active=" + active + ", deviceToken=" + deviceToken + ", userCreationDate=" + userCreationDate
                + ", userModificationDate=" + userModificationDate + "]";
    }
}