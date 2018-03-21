package com.infoclinika.mssharing.platform.entity;

import javax.persistence.*;

/**
 * @author : Alexander Serebriyan
 */

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ChangeEmailRequest {

    @Id
    @Column(name = "user_id")
    private Long userId;
    private String email;

    public ChangeEmailRequest(Long userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public ChangeEmailRequest() {
    }

    public String getEmail() {
        return email;
    }

    public Long getUserId() {
        return userId;
    }
}