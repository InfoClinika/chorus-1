package com.infoclinika.mssharing.model.helper;

import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Transactional
public interface RestHelper {

    public static long TOKEN_EXPIRATION_TIME = 1000L * 60L * 60L * 24L * 30L;//30 days

    UserDetails getUserDetailsByEmail(String email);

    UserDetails getUserDetails(long id);

    Token findAndProlongToken(String email);

    UserDetails getUserDetailsByToken(String token);

    Token generateToken(UserDetails userDetails);

    UserDetails checkToken(String token);

    void updateToken(Token token);

    void invalidateToken(Token token);

    boolean canUploadForInstrument(long instrumentId);

    class UserDetails {
        public final long id;
        public final String email;
        public final String passwordHash;
        public final Token token;
        public final boolean emailVerified;
        public final boolean hasLaboratories;

        public UserDetails(long id, String email, String passwordHash, Token token, boolean emailVerified,
                           boolean hasLaboratories) {
            this.id = id;
            this.email = email;
            this.passwordHash = passwordHash;
            this.token = token;
            this.emailVerified = emailVerified;
            this.hasLaboratories = hasLaboratories;
        }
    }

    class Token {
        public final long id;
        public final String token;
        public final Date expirationDate;

        public Token(long id, String token, Date expirationDate) {
            this.id = id;
            this.token = token;
            this.expirationDate = expirationDate;
        }

        @Override
        public String toString() {
            return "Token{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", expirationDate=" + expirationDate +
                '}';
        }
    }

}
