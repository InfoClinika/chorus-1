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
package com.infoclinika.mssharing.platform.model.testing.helper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.infoclinika.mssharing.platform.model.common.items.AdditionalExtensionImportance;
import com.infoclinika.mssharing.platform.model.common.items.FileExtensionItem;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.FileItemTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.MetaFactorTemplate;
import com.infoclinika.mssharing.platform.model.write.LabManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate.PersonInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.platform.model.common.items.AdditionalExtensionImportance.NOT_REQUIRED;

/**
 * Provides some reusable test data
 *
 * @author Stanislav Kurilin
 */
public class Data {
    public static final ImmutableSet<FileItemTemplate> NO_FILES = ImmutableSet.of();
    public static final ImmutableList<MetaFactorTemplate> NO_FACTORS = ImmutableList.of();
    public static final ImmutableList<String> NO_FACTOR_VALUES = ImmutableList.of();

    public static final String BOBS_EMAIL = "bob.marley@example.com";
    public static final String BOBS_FIRST = "Bob";
    public static final String BOBS_LAST = "Marley";
    public static final String BOBS_HASH = "123q";
    public static final String BOBS_PASS = "pwd";

    public static final String PAUL_FIRST_NAME = "Paul";
    public static final String PAUL_LAST_NAME = "Brown";
    public static final String PAUL_EMAIL = "paul.brown@example.com";

    public static final String KATE_FIRST_NAME = "Kate";
    public static final String KATE_LAST_NAME = "White";
    public static final String KATE_EMAIL = "kate.white@example.com";

    public static final String JOHN_FIRST_NAME = "John";
    public static final String JOHN_LAST_NAME = "Smith";
    public static final String JOHN_EMAIL = "john.smith@example.com";

    public static final String LAB_3 = "lab#3";
    public static final String LAB_4 = "lab#4";

    public static final String HARVARD_URL = "harward.com";
    public static final PersonInfo PAUL_INFO = new PersonInfo(PAUL_FIRST_NAME, PAUL_LAST_NAME, PAUL_EMAIL);
    public static final PersonInfo JOHN_INFO = new PersonInfo(JOHN_FIRST_NAME, JOHN_LAST_NAME, JOHN_EMAIL);
    public static final PersonInfo KATE_INFO = new PersonInfo(KATE_FIRST_NAME, KATE_LAST_NAME, KATE_EMAIL);
    public static final PersonInfo BOB_INFO = new PersonInfo(BOBS_FIRST, BOBS_LAST, BOBS_EMAIL);
    public static final PersonInfo L_PAUL_INFO = new PersonInfo(PAUL_FIRST_NAME, PAUL_LAST_NAME, PAUL_EMAIL);
    public static final PersonInfo L_KATE_INFO = new PersonInfo(KATE_FIRST_NAME, KATE_LAST_NAME, KATE_EMAIL);
    public static final PersonInfo L_BOB_INFO = new PersonInfo(BOBS_FIRST, BOBS_LAST, BOBS_EMAIL);

    public static final LabManagementTemplate.LabInfoTemplate LAB_4_DATA =
        new LabManagementTemplate.LabInfoTemplate(HARVARD_URL, L_PAUL_INFO, LAB_4);
    public static final LabManagementTemplate.LabInfoTemplate LAB_3_DATA =
        new LabManagementTemplate.LabInfoTemplate(HARVARD_URL, L_PAUL_INFO, LAB_3);
    public static final LabManagementTemplate.LabInfoTemplate LAB_2_DATA =
        new LabManagementTemplate.LabInfoTemplate("", L_PAUL_INFO, "");
    public static final ImmutableList<ExperimentManagementTemplate.AnnotationTemplate> EMPTY_ANNOTATIONS =
        ImmutableList.of();
    public static final String PROJECT_TITLE = "Running";
    public static final ImmutableMap<String, String> ANNOTATIONS = ImmutableMap.of("mykey", "myVal");
    public static final String studyType1 = "Mass Spectrometry";
    public static final String vendor1 = "Thermo";
    public static final String instrumentType11 = "Mass Spectrometer";
    public static final String instrumentType12 = "Spectrometer";
    public static final String vendor2 = "Agilent";
    public static final String vendor3 = "Waters";
    public static final String AB_SCIEX_INSTRUMENT_MODEL = "AB_SCIEX_MODEL";
    public static final String AB_SCIEX = "Sciex";
    public static final HashSet<FileExtensionItem> AB_SCIEX_EXTENSIONS = newHashSet(
        new FileExtensionItem(".wiff", ".wiff",
            Maps.newHashMap(
                ImmutableMap.of(
                    ".wiff.scan", NOT_REQUIRED,
                    ".wiff.mtd", NOT_REQUIRED
                )
            )
        ));
    public static final String BRUKER_INSTRUMENT_MODEL = "Bruker_MODEL";
    public static final String BRUKER = "Bruker";
    public static final HashSet<FileExtensionItem> BRUKER_EXTENSIONS = newHashSet(
        new FileExtensionItem(".d", ".d",
            Maps.newHashMap()
        ));
    public static final String instrumentType21 = "Mass Spectrometer";
    public static final String ADMIN_EMAIL = "mark@mmm.c";
    public static final String ADMIN_EMAIL_2 = "mark3@mmm.c";
    public static String RESEARCH_TITLE = "X1";
    public static String instrumentModel111 = "S1";
    public static String instrumentModel121 = "H1";
    public static String instrumentModel211 = "S1";
    public static String instrumentModel122 = "S1";
    public static String instrumentModel212 = "G";
    protected final ImmutableMap<String, String> annotations = of("MyKey", "MyVal");


}
