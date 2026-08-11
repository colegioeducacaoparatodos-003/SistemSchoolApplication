package com.SistemSchool.dto;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Objects;

public class PersonDTO {

    // DTO para criação
    public static class CreatePersonDTO {
        private String firstName;
        private String middleName;
        private String lastName;
        private String phone;
        private String address;
        private String city;
        private Double latitude;
        private Double longitude;
        private Integer fkUser;
        private String email;
        private String documentNumber;
        private String documentType;
        private boolean active = true;
        private String imagePerson;

        // Getters e Setters
        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getMiddleName() {
            return middleName;
        }

        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public Integer getFkUser() {
            return fkUser;
        }

        public void setFkUser(Integer fkUser) {
            this.fkUser = fkUser;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDocumentNumber() {
            return documentNumber;
        }

        public void setDocumentNumber(String documentNumber) {
            this.documentNumber = documentNumber;
        }

        public String getDocumentType() {
            return documentType;
        }

        public void setDocumentType(String documentType) {
            this.documentType = documentType;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public String getImagePerson() {
            return imagePerson;
        }

        public void setImagePerson(String imagePerson) {
            this.imagePerson = imagePerson;
        }

    }

    // DTO para atualização
    public static class UpdatePersonDTO {
        private int pkPerson;
        private String firstName;
        private String middleName;
        private String lastName;
        private String phone;
        private String address;
        private String city;
        private Double latitude;
        private Double longitude;
        private Integer fkUser;
        private String email;
        private String documentNumber;
        private String documentType;
        private Boolean active;
        private String imagePerson;

        // Getters e Setters
        public int getPkPerson() {
            return pkPerson;
        }

        public void setPkPerson(int pkPerson) {
            this.pkPerson = pkPerson;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getMiddleName() {
            return middleName;
        }

        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public Integer getFkUser() {
            return fkUser;
        }

        public void setFkUser(Integer fkUser) {
            this.fkUser = fkUser;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDocumentNumber() {
            return documentNumber;
        }

        public void setDocumentNumber(String documentNumber) {
            this.documentNumber = documentNumber;
        }

        public String getDocumentType() {
            return documentType;
        }

        public void setDocumentType(String documentType) {
            this.documentType = documentType;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        public String getImagePerson() {
            return imagePerson;
        }

        public void setImagePerson(String imagePerson) {
            this.imagePerson = imagePerson;
        }
    }

    // DTO para resposta
    public static class PersonResponseDTO {
        private Integer pkPerson;
        private String firstName;
        private String middleName;
        private String lastName;
        private String phone;
        private String address;
        private String city;
        private Double latitude;
        private Double longitude;
        private int fkUser;
        private String imagePerson;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String email;
        private String documentNumber;
        private String documentType;
        private boolean active;
        private String fullName;
        private String initials;

        public PersonResponseDTO() {
        }

        public PersonResponseDTO(Integer pkPerson, String firstName, String middleName, String lastName,
                String documentNumber, String imagePerson) {
            this.pkPerson = pkPerson;
            this.firstName = firstName;
            this.middleName = middleName;
            this.lastName = lastName;
            this.documentNumber = documentNumber;
            this.imagePerson = imagePerson;
        }

        // Getters e Setters
        public Integer getPkPerson() {
            return pkPerson;
        }

        public void setPkPerson(Integer pkPerson) {
            this.pkPerson = pkPerson;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getMiddleName() {
            return middleName;
        }

        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public int getFkUser() {
            return fkUser;
        }

        public void setFkUser(int fkUser) {
            this.fkUser = fkUser;
        }

        public String getImagePerson() {
            return imagePerson;
        }

        public void setImagePerson(String imagePerson) {
            this.imagePerson = imagePerson;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDocumentNumber() {
            return documentNumber;
        }

        public void setDocumentNumber(String documentNumber) {
            this.documentNumber = documentNumber;
        }

        public String getDocumentType() {
            return documentType;
        }

        public void setDocumentType(String documentType) {
            this.documentType = documentType;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getInitials() {
            return initials;
        }

        public void setInitials(String initials) {
            this.initials = initials;
        }

        public String getFullSearchName() {
            String mid = (middleName != null && !middleName.isEmpty()) ? middleName + " " : "";
            return firstName + " " + mid + lastName;
        }
    }

    // DTO para busca com filtros
    public static class PersonFilterDTO {
        private String firstName;
        private String lastName;
        private String phone;
        private String email;
        private String city;
        private String documentNumber;
        private Boolean active;
        private Integer fkUser;
        private Date createdAfter;
        private Date createdBefore;

        // Getters e Setters
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

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getDocumentNumber() {
            return documentNumber;
        }

        public void setDocumentNumber(String documentNumber) {
            this.documentNumber = documentNumber;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        public Integer getFkUser() {
            return fkUser;
        }

        public void setFkUser(Integer fkUser) {
            this.fkUser = fkUser;
        }

        public Date getCreatedAfter() {
            return createdAfter;
        }

        public void setCreatedAfter(Date createdAfter) {
            this.createdAfter = createdAfter;
        }

        public Date getCreatedBefore() {
            return createdBefore;
        }

        public void setCreatedBefore(Date createdBefore) {
            this.createdBefore = createdBefore;
        }
    }
}
