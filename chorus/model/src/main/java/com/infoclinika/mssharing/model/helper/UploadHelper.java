/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.helper;

import com.google.common.collect.ImmutableSet;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Stanislav Kurilin
 */
@Deprecated
@Transactional(readOnly = true)
public interface UploadHelper {

    @Deprecated
    ImmutableSet<Long> incompleteFiles(long actor, long instrument);
}
