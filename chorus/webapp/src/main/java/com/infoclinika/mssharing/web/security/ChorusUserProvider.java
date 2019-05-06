package com.infoclinika.mssharing.web.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author vladimir.moiseiev.
 */
public interface ChorusUserProvider extends UserDetailsService {
    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    UserDetails getUserDetails(long id);
}
