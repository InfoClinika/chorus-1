package com.infoclinika.mssharing.model.write;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Vladislav Kovchug
 */
@Transactional
public interface ClientTokenService {

    ClientToken generateTokenForUser(long id);

    Long readUserByToken(ClientToken clientToken);

    class ClientToken {
        public final String token;

        public ClientToken(String token) {
            this.token = token;
        }
    }

}
