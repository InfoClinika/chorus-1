package com.infoclinika.sso.exception;

import javax.security.auth.login.AccountException;

/**
 * @author andrii.loboda
 */
public class AccountIsAlreadyLinkedAccountException extends AccountException {
    private static final long serialVersionUID = 2734056080358933129L;

    public AccountIsAlreadyLinkedAccountException(String msg) {
        super(msg);
    }
}
