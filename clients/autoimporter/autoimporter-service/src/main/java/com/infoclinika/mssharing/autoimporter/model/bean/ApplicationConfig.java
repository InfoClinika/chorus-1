package com.infoclinika.mssharing.autoimporter.model.bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.infoclinika.mssharing.dto.request.UserNamePassDTO;

/**
 * author Ruslan Duboveckij
 */
public class ApplicationConfig {
    public final String username;
    public final String password;

    @JsonCreator
    public ApplicationConfig(@JsonProperty("username") String username,
                             @JsonProperty("password") String password) {
        this.username = username;
        this.password = password;
    }

    @JsonIgnore
    public UserNamePassDTO getUserNamePass() {
        return new UserNamePassDTO(username, password);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("username", username)
            .add("password", password)
            .toString();
    }
}
