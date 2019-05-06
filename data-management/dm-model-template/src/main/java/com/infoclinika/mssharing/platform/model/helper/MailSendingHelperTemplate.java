package com.infoclinika.mssharing.platform.model.helper;

import java.util.List;

/**
 * @author Herman Zamula
 */
public interface MailSendingHelperTemplate {

    UserDetails userDetails(long id);

    String projectName(long id);

    String instrumentName(long instrument);

    String labName(long lab);

    String experimentName(long experiment);

    List<String> labMembersEmails(long labId);

    class UserDetails {
        public final String name;
        public final String email;

        public UserDetails(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }


}
