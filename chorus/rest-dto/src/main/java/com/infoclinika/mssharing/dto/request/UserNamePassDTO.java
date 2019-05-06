package com.infoclinika.mssharing.dto.request;

/**
 * author: Ruslan Duboveckij
 */
public class UserNamePassDTO {

    private String username;
    private String password;

    public UserNamePassDTO() {
    }

    public UserNamePassDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "UserNamePassDTO{" +
            "username='" + username + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserNamePassDTO that = (UserNamePassDTO) o;

        if (username != null ? !username.equals(that.username) : that.username != null) {
            return false;
        }
        return !(password != null ? !password.equals(that.password) : that.password != null);

    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
}
