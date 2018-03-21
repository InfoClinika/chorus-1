package com.infoclinika.mssharing.model.internal.entity;


import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "rest_token")
public class RestToken {

    @Id
    private Long id;

    @NotNull
    private String token;

    private Date expirationDate;

    public RestToken() {
    }

    public RestToken(Long id, String token, Date expirationDate) {
        this.id = id;
        this.token = token;
        this.expirationDate = expirationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}
