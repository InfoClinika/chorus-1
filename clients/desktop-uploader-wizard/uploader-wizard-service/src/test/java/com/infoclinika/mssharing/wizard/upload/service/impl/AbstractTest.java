// Copyright (c) 2016, NanoString Technologies, Inc.  All rights reserved.
// Use of this file for any purpose requires prior written consent of NanoString Technologies, Inc.

package com.infoclinika.mssharing.wizard.upload.service.impl;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

/**
 * @author Yevhen Panko (yevhen.panko@teamdev.com)
 */
@ContextConfiguration("classpath:wizard_context_service.xml" )
public class AbstractTest extends AbstractTestNGSpringContextTests {
    //Supported vendors 11/11/2016
    protected static final String[] SUPPORTED_VENDORS = new String[]{
            "Thermo Scientific",
            "Waters",
            "Agilent",
            "Bruker",
            "Sciex",
            "Affymetrix",
            "Illumina",
            "Wyatt"
    };
}
