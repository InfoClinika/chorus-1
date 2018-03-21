package com.infoclinika.mssharing.integration.test.preconditions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Alexander Orlov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LoginRequired {
    String email() default "pavel.kaplin@gmail.com";
    String password() default "pwd";
    boolean isDisableBillingNotification() default true;
}
