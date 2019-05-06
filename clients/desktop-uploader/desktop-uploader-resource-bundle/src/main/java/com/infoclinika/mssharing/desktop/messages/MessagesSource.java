package com.infoclinika.mssharing.desktop.messages;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author timofey.kasyanov
 *     date 26.03.14.
 */
public abstract class MessagesSource {

    private static final String RESOURCE_BASE_NAME = "messages/messages";
    private static ResourceBundle resourceBundle;

    private MessagesSource() {}

    public static void setLocale(Locale locale) {

        resourceBundle = ResourceBundle.getBundle(RESOURCE_BASE_NAME, locale);

    }

    public static String getMessage(MessageKey key) {

        if (resourceBundle == null) {
            resourceBundle = ResourceBundle.getBundle(RESOURCE_BASE_NAME);
        }

        return resourceBundle.getString(key.getKey());

    }

    public static void main(String[] args) {

        final String message = getMessage(MessageKey.MAIN_TITLE);

        System.out.println(message);

    }

}
