package com.infoclinika.mssharing.platform.model.helper;

/**
 * @author Herman Zamula
 */
public interface MailSendingHelperTemplate {

    UserDetails userDetails(long id);

    String projectName(long id);

    String instrumentName(long instrument);

    String labName(long lab);

    String experimentName(long experiment);

    class UserDetails {
        public final String name;
        public final String email;

        public UserDetails(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }

}
