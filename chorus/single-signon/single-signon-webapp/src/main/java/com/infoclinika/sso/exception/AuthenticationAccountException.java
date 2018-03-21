package com.infoclinika.sso.exception;

import javax.security.auth.login.AccountException;

/**
 * @author andrii.loboda
 */
public class AuthenticationAccountException extends AccountException {
    private static final long serialVersionUID = -4185911259916157644L;

    public AuthenticationAccountException(String msg) {
        super(msg);
    }
}
