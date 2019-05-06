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
package com.infoclinika.mssharing.model.write;

import com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate;

/**
 * Project owner can share his project to application users and the sharing groups. Group is just a custom set of users.
 * In this case of sharing project all the experiments and the RAW files which are the part of this project
 * become available for those users for read access.
 * <p/>
 * <p/>
 * Also project owner can mark his project as public in this case it's become available to all system users.
 *
 * @author Stanislav Kurilin
 */
public interface SharingManagement extends SharingManagementTemplate {

}
