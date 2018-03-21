package com.infoclinika.mssharing.web.security;

import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.write.UserManagement;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate.PersonInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

import javax.inject.Inject;

/**
 * @author vladimir.moiseiev.
 */

//@Component
public class SamlUserProvider implements ChorusUserProvider, SAMLUserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(SamlUserProvider.class);
    private static final String NAME_ID_FORMAT_EMAIL = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String EMAIL_ADDRESS = "emailAddress";
    private static final String EMAIL_ADDRESS_SMALL = "emailaddress";
    private static final String EMAIL_ADDRESS_SHORT = "Email";
    private static final String FIRST_NAME = "firstName";
    private static final String FIRST_NAME2 = "FirstName";
    private static final String LAST_NAME = "lastName";
    private static final String LAST_NAME2 = "LastName";
    private static final String SAML_DISPLAY_NAME = "SAML_DISPLAYNAME";
    @Inject
    private UserManagement userManagement;
    @Inject
    private SecurityHelper securityHelper;

    @Override
    public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {

        final String nameId = credential.getNameID().getValue();
        final String email = getEmail(credential, nameId);

        final UserInfo userInfo = getUserInfo(credential);
        LOG.info("# User info from SAML credential: " + userInfo);

        final String firstName = userInfo.firstName;
        final String lastName = userInfo.lastName;

        final SecurityHelper.UserDetails userDetails = securityHelper.getUserDetailsByEmail(email);

        LOG.info("# User details: " + userDetails);

        if (userDetails == null) {
            final PersonInfo personInfo = new PersonInfo(firstName, lastName, email);

            final long userId = userManagement.createPersonAndApproveMembership(personInfo, "pwd", (Long) null, null);
            userManagement.verifyEmail(userId);

            final SecurityHelper.UserDetails detailsByEmail = securityHelper.getUserDetailsByEmail(email);
            return UserProviderHelper.USER_DETAILS_TRANSFORMER.apply(detailsByEmail);
        }

        if (StringUtils.isNotBlank(firstName) && StringUtils.isNotBlank(lastName)&&
                (!userDetails.firstName.equals(firstName) || !userDetails.lastName.equals(lastName))) {

            userManagement.changeFirstName(userDetails.id, firstName);
            userManagement.changeLastName(userDetails.id, lastName);

            final SecurityHelper.UserDetails changedUserDetails = securityHelper.getUserDetailsByEmail(email);
            return UserProviderHelper.USER_DETAILS_TRANSFORMER.apply(changedUserDetails);
        }

        return UserProviderHelper.USER_DETAILS_TRANSFORMER.apply(userDetails);
    }

    private UserInfo getUserInfo(SAMLCredential credential) {
        String firstNameAttribute = credential.getAttributeAsString(FIRST_NAME);
        if (firstNameAttribute == null) {
            firstNameAttribute = credential.getAttributeAsString(FIRST_NAME2);
        }

        String lastNameAttribute = credential.getAttributeAsString(LAST_NAME);
        if (lastNameAttribute == null) {
            lastNameAttribute = credential.getAttributeAsString(LAST_NAME2);
        }

        if (firstNameAttribute == null || lastNameAttribute == null) {
            final String displayName = credential.getAttributeAsString(SAML_DISPLAY_NAME);

            if (displayName != null && !displayName.isEmpty()) {
                final int firstSpace = displayName.indexOf(' ');
                firstNameAttribute = displayName.substring(0, firstSpace);
                lastNameAttribute = displayName.substring(firstSpace + 1);
            }
        }

        if (firstNameAttribute == null && lastNameAttribute == null) {
            final String errorText = "No First and Last names in SAML response";
            LOG.error(errorText);
           throw new RuntimeException(errorText);
        }
        return new UserInfo(firstNameAttribute, lastNameAttribute);
    }

    private String getEmail(SAMLCredential credential, String nameId) {
        String email;
        if (NAME_ID_FORMAT_EMAIL.equals(credential.getNameID().getFormat())) {
            email = nameId;
        } else {
            String emailAddress = credential.getAttributeAsString(EMAIL_ADDRESS);

            //Celgene
            if (emailAddress == null) {
                emailAddress = credential.getAttributeAsString(EMAIL_ADDRESS_SMALL);
            }

            //Merck
            if (emailAddress == null) {
                emailAddress = credential.getAttributeAsString(EMAIL_ADDRESS_SHORT);
            }

            if (emailAddress == null) {
                final String errorText = "No email in SAML response";
                LOG.error(errorText);
                throw new RuntimeException(errorText);
            }
            email = emailAddress;
        }
        return email;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    @Override
    public UserDetails getUserDetails(long id) {
        return null;
    }

    private static final class UserInfo {
        public String firstName;
        public String lastName;
        public UserInfo(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        @Override
        public String toString() {
            return "UserInfo{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    '}';
        }
    }
}
