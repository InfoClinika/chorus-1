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
package com.infoclinika.mssharing.web.controller.request;

import com.infoclinika.mssharing.model.write.ExperimentCategory;

/**
 * @author Oleksii Tymchenko
 */
public class StartProteinDatabaseUploadRequest {
    public long dbType;
    public String name;
    public String filename;
    public long sizeInBytes;
    public boolean bPublic;
    public boolean bReversed;
    public ExperimentCategory category;
    public long accessionParseRule;
    public long descriptionParseRule;

    @Override
    public String toString() {
        return "StartProteinFastaDatabaseUploadRequest{" +
            "filename='" + filename + '\'' +
            "name='" + name + '\'' +
            ", dbType=" + dbType +
            ", bPublic=" + bPublic +
            ", bReversed=" + bReversed +
            ", sizeInBytes=" + sizeInBytes +
            ", category=" + category +
            ", accessionParseRule=" + accessionParseRule +
            ", descriptionParseRule=" + descriptionParseRule +
            '}';
    }
}
