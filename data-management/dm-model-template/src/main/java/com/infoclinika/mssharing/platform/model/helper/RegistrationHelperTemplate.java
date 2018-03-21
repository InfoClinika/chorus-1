/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.model.helper;

import com.google.common.collect.ImmutableSortedSet;

/**
 * @author Stanislav Kurilin, Herman Zamula
 */
public interface RegistrationHelperTemplate<LAB_ITEM extends RegistrationHelperTemplate.LabItem> {
    /**
     * Find out if the user with the specified email has already been registered.
     *
     * @param email the email of user to be tested for previous registration
     * @return true if user with such email was registered, false otherwise
     */
    boolean isEmailAvailable(String email);

    boolean isEmailActivated(String email);

    ImmutableSortedSet<LAB_ITEM> availableLabs();

    class LabItem {
        public final long id;
        public final String name;

        public LabItem(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
