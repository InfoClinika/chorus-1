package com.infoclinika.mssharing.integration.test.data;

/**
 * @author Sergii Moroz
 */
public class UserData {

    private final String email;
    private final String emailConfirmation;
    private final String password;
    private final String passwordConfirmation;
    private final String firstName;
    private final String lastName;

    private UserData(Builder builder) {
        this.email = builder.email;
        this.emailConfirmation = builder.emailConfirmation;
        this.password = builder.password;
        this.passwordConfirmation = builder.passwordConfirmation;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
    }

    public String getEmailConfirmation() {
        return emailConfirmation;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public static class Builder {
        private String email;
        private String emailConfirmation;
        private String password;
        private String passwordConfirmation;
        private String firstName;
        private String lastName;

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder emailConfirmation(String emailConfirmation) {
            this.emailConfirmation = emailConfirmation;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder passwordConfirmation(String passwordConfirmation) {
            this.passwordConfirmation = passwordConfirmation;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserData build() {
            return new UserData(this);
        }
    }

    @Override
    public String toString() {
        return "UserData{" +
                "email='" + email + '\'' +
                ", emailConfirmation='" + emailConfirmation + '\'' +
                ", password='" + password + '\'' +
                ", passwordConfirmation='" + passwordConfirmation + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
