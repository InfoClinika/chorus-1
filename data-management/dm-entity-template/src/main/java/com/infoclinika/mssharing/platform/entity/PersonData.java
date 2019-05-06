package com.infoclinika.mssharing.platform.entity;

import org.hibernate.annotations.Index;

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Embeddable
public class PersonData {
    @Index(name = "USER_EMAIL_IDX")
    @Basic(optional = false)
    String email;
    @Index(name = "USER_FIRST_NAME_IDX")
    @Basic(optional = false)
    String firstName;
    @Basic(optional = false)
    String lastName;

    public PersonData(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    protected PersonData() {
    }

    @Transient
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }
}