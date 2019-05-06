package com.infoclinika.sso.exception;

import javax.security.auth.login.AccountException;

/**
 * @author andrii.loboda
 */
public class ApplicationAuthenticationServerErrorAccountException extends AccountException {
    private static final long serialVersionUID = -1847613103684166412L;

    public ApplicationAuthenticationServerErrorAccountException(String msg) {
        super(msg);
    }

}
