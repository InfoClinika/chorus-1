package com.infoclinika.mssharing.integration.test.preconditions;

import com.infoclinika.mssharing.integration.test.data.EmailFolder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Alexander Orlov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CleanEmailBoxBefore {
    EmailFolder[] folderName() default EmailFolder.VERIFY_EMAIL;
}
