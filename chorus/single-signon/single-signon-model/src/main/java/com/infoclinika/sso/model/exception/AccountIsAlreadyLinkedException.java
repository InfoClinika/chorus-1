package com.infoclinika.sso.model.exception;

/**
 * @author andrii.loboda
 */
public class AccountIsAlreadyLinkedException extends RuntimeException {
    private static final long serialVersionUID = -4573511985677966545L;

    public AccountIsAlreadyLinkedException(String message) {
        super(message);
    }
}
