package com.infoclinika.mssharing.platform.model.read;

import java.util.List;

/**
 * @author : Alexander Serebriyan
 */
public interface UserReaderTemplate<USER_LINE extends UserReaderTemplate.UserLineTemplate> {

    List<USER_LINE> readUsersByLab(long actor, long labId);

    class UserLineTemplate {
        public final long id;
        public final String email;
        public final String firstName;
        public final String lastName;

        public UserLineTemplate(long id, String email, String firstName, String lastName) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }
}
