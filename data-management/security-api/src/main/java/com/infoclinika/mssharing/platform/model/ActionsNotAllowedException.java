package com.infoclinika.mssharing.platform.model;

/**
 * @author timofei.kasianov 7/30/18
 */
public class ActionsNotAllowedException extends AccessDenied {

    private static final String ERROR_MESSAGE_TEMPLATE =
        "User with ID: {%d} is not allowed to perform any actions in the system.";

    public ActionsNotAllowedException(long actor) {
        super(String.format(ERROR_MESSAGE_TEMPLATE, actor));
    }
}
