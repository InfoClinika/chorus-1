package com.infoclinika.mssharing.integration.test.preconditions;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Alexander Orlov
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
public @interface AfterTestComplete {
}
