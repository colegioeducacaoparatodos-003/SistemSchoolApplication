package com.SistemSchool.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.primefaces.model.file.UploadedFile;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Objects;

@Entity
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int pkPerson;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private String address;
    private String city;
    private int fkUser;
    private String imagePerson;

    @Transient
    private UploadedFile imagePersonUtil;

    private String email;

    public Person() {
    }

    public Person(int pkPerson, String firstName, String middleName, String lastName, String phone, String address,
            String city, int fkUser, String imagePerson,
            UploadedFile imagePersonUtil, String email) {
        this.pkPerson = pkPerson;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.fkUser = fkUser;
        this.imagePerson = imagePerson;
        this.imagePersonUtil = imagePersonUtil;
        this.email = email;
    }

    public int getPkPerson() {
        return this.pkPerson;
    }

    public void setPkPerson(int pkPerson) {
        this.pkPerson = pkPerson;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return this.middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }
    public int getFkUser() {
        return this.fkUser;
    }

    public void setFkUser(int fkUser) {
        this.fkUser = fkUser;
    }

    public String getImagePerson() {
        return this.imagePerson;
    }

    public void setImagePerson(String imagePerson) {
        this.imagePerson = imagePerson;
    }

    public UploadedFile getImagePersonUtil() {
        return this.imagePersonUtil;
    }

    public void setImagePersonUtil(UploadedFile imagePersonUtil) {
        this.imagePersonUtil = imagePersonUtil;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Person pkPerson(int pkPerson) {
        setPkPerson(pkPerson);
        return this;
    }

    public Person firstName(String firstName) {
        setFirstName(firstName);
        return this;
    }

    public Person middleName(String middleName) {
        setMiddleName(middleName);
        return this;
    }

    public Person lastName(String lastName) {
        setLastName(lastName);
        return this;
    }

    public Person phone(String phone) {
        setPhone(phone);
        return this;
    }

    public Person address(String address) {
        setAddress(address);
        return this;
    }

    public Person city(String city) {
        setCity(city);
        return this;
    }

    public Person fkUser(int fkUser) {
        setFkUser(fkUser);
        return this;
    }

    public Person imagePerson(String imagePerson) {
        setImagePerson(imagePerson);
        return this;
    }

    public Person imagePersonUtil(UploadedFile imagePersonUtil) {
        setImagePersonUtil(imagePersonUtil);
        return this;
    }

    public Person email(String email) {
        setEmail(email);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkPerson, firstName, middleName, lastName, phone, address, city,
                fkUser, imagePerson, imagePersonUtil, email);
    }

    @Override
    public String toString() {
        return "{" +
                " pkPerson='" + getPkPerson() + "'" +
                ", firstName='" + getFirstName() + "'" +
                ", middleName='" + getMiddleName() + "'" +
                ", lastName='" + getLastName() + "'" +
                ", phone='" + getPhone() + "'" +
                ", address='" + getAddress() + "'" +
                ", city='" + getCity() + "'" +
                ", fkUser='" + getFkUser() + "'" +
                ", imagePerson='" + getImagePerson() + "'" +
                ", imagePersonUtil='" + getImagePersonUtil() + "'" +
                ", email='" + getEmail() + "'" +
                "}";
    }

}
