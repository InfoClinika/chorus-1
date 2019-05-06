package com.infoclinika.mssharing.platform.web.security;

import com.google.common.collect.ImmutableSet;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.security.Principal;
import java.util.Collection;
import java.util.Set;

/**
 * @author Pavel Kaplin
 */
public class RichUser extends User {

    private final String firstName;
    private final String lastName;
    private final long id;
    private final ImmutableSet<Long> labs;

    public RichUser(long id,
                    String username,
                    String password,
                    Collection<? extends GrantedAuthority> authorities,
                    String firstName,
                    String lastName,
                    boolean enabled,
                    Set<Long> labs) {
        super(username, password, enabled, true, true, true, authorities);
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.labs = ImmutableSet.copyOf(labs);
    }

    public static RichUser get(Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("User is unauthorized");
        }
        // inspired by http://stackoverflow.com/questions/8764545/best-practice-for-getting-active-users-userdetails
        return (RichUser) ((Authentication) principal).getPrincipal();
    }

    public static long getUserId(Principal principal) {
        return get(principal).getId();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public long getId() {
        return id;
    }

    public Set<Long> getLabs() {
        return labs;
    }

    public static long getCurrentUserId() {
        return getUserId(SecurityContextHolder.getContext().getAuthentication());
    }
}
