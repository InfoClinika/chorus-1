package com.infoclinika.mssharing.web.security;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author andrii.loboda
 */
public class UserDetailsByCasTokenServiceWrapper
    extends UserDetailsByNameServiceWrapper<CasAssertionAuthenticationToken> {
    public static final String ATTRIBUTE_CHORUS_USERNAME = "chorus-username";
    public static final String ATTRIBUTE_CHORUS_ID = "chorus-id";
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsByCasTokenServiceWrapper.class);
    private UserDetailsService userDetailsService;

    /**
     * Constructs an empty wrapper for compatibility with Spring Security 2.0.x's method
     * of using a setter.
     */
    public UserDetailsByCasTokenServiceWrapper() {
        // constructor for backwards compatibility with 2.0
    }

    /**
     * Constructs a new wrapper using the supplied
     * {@link org.springframework.security.core.userdetails.UserDetailsService} as the
     * service to delegate to.
     *
     * @param userDetailsService the UserDetailsService to delegate to.
     */
    public UserDetailsByCasTokenServiceWrapper(final UserDetailsService userDetailsService) {
        Assert.notNull(userDetailsService, "userDetailsService cannot be null.");
        this.userDetailsService = userDetailsService;
    }

    /**
     * Check whether all required properties have been set.
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.userDetailsService, "UserDetailsService must be set");
    }

    @Override
    public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
        final AttributePrincipal principal = token.getAssertion().getPrincipal();
        final Map<String, Object> attributes = principal.getAttributes();
        for (Map.Entry<String, Object> attributeEntry : attributes.entrySet()) {
            LOGGER.info("Got attribute with key: " + attributeEntry.getKey() + ", value: " + attributeEntry.getValue());
        }
        LOGGER.info("Principal:" + principal.getName());
        final String username = (String) attributes.get(ATTRIBUTE_CHORUS_USERNAME);
        checkNotNull(
            username,
            "Chorus authorization cannot be done because of %s attribute absence.",
            ATTRIBUTE_CHORUS_USERNAME
        );
        return userDetailsService.loadUserByUsername(username);
    }

    @Override
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
}
