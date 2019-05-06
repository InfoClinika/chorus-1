package com.infoclinika.sso.account.linking;

import com.infoclinika.sso.UserCredentialInForm;
import com.infoclinika.sso.model.ApplicationType;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

/**
 * @author andrii.loboda
 */
public interface LinkingDetailsProvider {
    boolean isAccountLinked(UserCredentialInForm credentials);

    LinkingUserDetails getUserDetailsForLinking(UserCredentialInForm credentials);

    class LinkingUserDetails implements Serializable {
        private static final long serialVersionUID = -6978150321154360218L;
        public final long id;
        public final List<ApplicationType> applicationTypesToLink = newLinkedList();

        public LinkingUserDetails(long id, Collection<ApplicationType> applicationTypesToLink) {
            this.id = id;
            this.applicationTypesToLink.addAll(applicationTypesToLink);
        }

    }
}
