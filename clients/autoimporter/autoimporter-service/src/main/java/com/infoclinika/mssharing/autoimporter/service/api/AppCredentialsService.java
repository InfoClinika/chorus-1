package com.infoclinika.mssharing.autoimporter.service.api;

import com.infoclinika.mssharing.dto.request.UserNamePassDTO;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;

import java.util.List;

/**
 * author Ruslan Duboveckij
 */
public interface AppCredentialsService {
    AppCredentials read();

    void readContext(List<InstrumentDTO> instruments, List<DictionaryDTO> species);

    void save();

    class AppCredentials {
        private UserNamePassDTO userNamePass;
        private String token;

        public AppCredentials() {
        }

        public AppCredentials(UserNamePassDTO userNamePass, String token) {
            this.userNamePass = userNamePass;
            this.token = token;
        }

        public UserNamePassDTO getUserNamePass() {
            return userNamePass;
        }

        public String getToken() {
            return token;
        }
    }
}
