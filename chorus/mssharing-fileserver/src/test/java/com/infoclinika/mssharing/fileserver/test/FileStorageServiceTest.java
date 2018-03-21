package com.infoclinika.mssharing.fileserver.test;/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.infoclinika.mssharing.platform.fileserver.impl.FileStorageService;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.fileserver.model.StoredFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.*;

import static org.testng.Assert.*;

/**
 * @author Oleksii Tymchenko
 */
@ContextConfiguration(locations = {"classpath:fileserver-test.cfg.xml"})
public class FileStorageServiceTest extends AbstractTestNGSpringContextTests {

    @Inject
    private FileStorageService storageService;


    @Test(enabled = false)
    public void testGetNonExistingFile() {
        final NodePath randomPath = new NodePath("non-existing-node-path" + System.currentTimeMillis());
        final StoredFile file = storageService.get(randomPath);
        assertNull(file, "File obtained at random path expected to be NULL, but it is not.");
    }

    @Test(enabled = false)
    public void testPutGet() {
        final File tempDir = Files.createTempDir();
        final String filename = "testPutGet" + System.currentTimeMillis() + "test.tmp";
        final File file = new File(tempDir, filename);
        try {
            Files.touch(file);
            Files.write("My test file contents".getBytes(), file);

            final NodePath nodePath = new NodePath("myuser/" + filename);
            final StoredFile storedFile = new StoredFile(new BufferedInputStream(new FileInputStream(file)));
            storageService.put(nodePath, storedFile);

            final StoredFile obtainedFile = storageService.get(nodePath);
            assertNotNull(obtainedFile, "Obtained file must not be NULL at path " + nodePath);

            final File tempDestFile = File.createTempFile("unit-test", null);
            final FileOutputStream fos = new FileOutputStream(tempDestFile);

            InputStream inputStream = obtainedFile.getInputStream();
            ByteStreams.copy(inputStream, fos);
            fos.flush();

            final boolean filesAreTheSame = Files.equal(file, tempDestFile);
            assertTrue(filesAreTheSame, "Source file and obtained file must have the same contents");
        } catch (IOException e) {
            fail("Something terrible has happened during the test", e);
        } finally {
            if (file.exists()) {
                file.deleteOnExit();
            }
        }

    }

}
