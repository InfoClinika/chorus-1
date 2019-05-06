package com.infoclinika.mssharing.web.security;

import com.google.common.base.Function;
import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.platform.web.security.RichUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.List;

/**
 * @author vladimir.moiseiev.
 */
public class UserProviderHelper {
    private static final String ROLE_USER = "ROLE_user";
    private static final SimpleGrantedAuthority[] USER_ROLES = {
        new SimpleGrantedAuthority(ROLE_USER)
    };
    private static final String ROLE_ADMIN = "ROLE_admin";
    private static final SimpleGrantedAuthority[] ADMIN_ROLES = {
        new SimpleGrantedAuthority(ROLE_ADMIN),
        new SimpleGrantedAuthority(ROLE_USER)
    };
    private static final String ROLE_LAB_HEAD = "ROLE_labHead";
    private static final SimpleGrantedAuthority[] LAB_HEAD_ROLES = {
        new SimpleGrantedAuthority(ROLE_USER),
        new SimpleGrantedAuthority(ROLE_LAB_HEAD)
    };

    static final Function<SecurityHelper.UserDetails, UserDetails> USER_DETAILS_TRANSFORMER =
        input -> new RichUser(
            input.id,
            input.email,
            input.password,
            getRoles(input.admin, input.labHead),
            input.firstName,
            input.lastName,
            input.emailVerified,
            input.labs
        );

    public static List<SimpleGrantedAuthority> getRoles(boolean isAdmin, boolean labHead) {
        if (isAdmin) {
            return Arrays.asList(ADMIN_ROLES);
        }
        if (labHead) {
            return Arrays.asList(LAB_HEAD_ROLES);
        }

        return Arrays.asList(USER_ROLES);
    }
}
