/*
package com.infoclinika.mssharing.autoimporter.service;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.autoimporter.model.Context;
import com.infoclinika.mssharing.autoimporter.model.bean.UploadItem;
import com.infoclinika.mssharing.autoimporter.service.composer.ComposerFactory;
import com.infoclinika.mssharing.autoimporter.service.util.AbstractTestSuite;
import com.infoclinika.mssharing.autoimporter.service.util.ConfigBean;
import com.infoclinika.mssharing.autoimporter.service.util.FileCreator;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

*/
/**
 * author Ruslan Duboveckij
 *//*

public class ComposerTest extends AbstractTestSuite {
    private static final Predicate<UploadItem> FILE_EXISTS = new Predicate<UploadItem>() {
        @Override
        public boolean apply(@Nullable UploadItem input) {
            return input != null && input.getFile().exists();
        }
    };
    @Inject
    private ComposerFactory factory;
    @Inject
    private ConfigBean configBean;

    @Test
    public void testThermo() throws Exception {
        Assert.assertFalse("Error - not found impl",
                factory.compose(createContext(instrumentThermo0),
                        twoRawFiles1).isEmpty());
    }

    //@Test
    public void testWatersAndAgilent() throws Exception {
        List<File> it1 = Lists.newArrayList(folderMany1);
        final File folder1 = FileCreator.createTempFolder(".d");
        final File folder2 = FileCreator.createTempFolder(".d");
        FileCreator.createTempFile(folder1);
        FileCreator.createTempFile(folder2);
        List<File> it2 = Lists.newArrayList(folder1, folder2);
        Assert.assertTrue("Error - not found impl Waters",
                FluentIterable.from(factory.compose(createContext(instrumentWaters), it1)).allMatch(FILE_EXISTS));
        Assert.assertTrue("Error - not found impl Agilent",
                FluentIterable.from(factory.compose(createContext(instrumentAgilent), it2)).allMatch(FILE_EXISTS));
    }

    //@Test
    public void testBruker() throws Exception {
        File folderBruker = FileCreator.createFolder("UploadFolderBruker");

        File folderRegions1 = FileCreator.createFolder(folderBruker, "folderRegions1.d");
        FileCreator.createFile(folderRegions1, "test.test");
        FileCreator.createFile(folderBruker, "folderRegions1.dat");
        FileCreator.createFile(folderBruker, "folderRegions1.mis");

        File folderRegions2 = FileCreator.createFolder(folderBruker, "folderRegions2.d");
        FileCreator.createFile(folderRegions2, "test.test");
        FileCreator.createFile(folderBruker, "folderRegions2.dat");
        FileCreator.createFile(folderBruker, "folderRegions2.mis");

        List<File> it1 = Lists.newArrayList(folderBruker.listFiles());
        List<UploadItem> compose = factory.compose(createContext(instrumentBruker), it1);
        Assert.assertTrue("Error - not found impl Bruker", Iterables.all(compose, FILE_EXISTS));
        Assert.assertEquals("Error - not properly archived Bruker", 2, compose.size());

        final ZipFile zipFile = new ZipFile(compose.get(0).getFile());
        final int size = zipFile.size();

        zipFile.close();

        Assert.assertEquals("Error - not all file archived", 3, size);
    }

    //@Test
    public void testAbSciex() throws Exception {
        File folderAbSciex = FileCreator.createFolder("UploadFolderTestAbSciex");
        FileCreator.createFile(folderAbSciex, "test1.wiff.scan");
        FileCreator.createFile(folderAbSciex, "test1.wiff");
        FileCreator.createFile(folderAbSciex, "test1.wiff.mtd");
        FileCreator.createFile(folderAbSciex, "test2.wiff.scan");
        FileCreator.createFile(folderAbSciex, "test2.wiff");
        FileCreator.createFile(folderAbSciex, "test2.wiff.mtd");

        List<File> it1 = Lists.newArrayList(folderAbSciex.listFiles());
        List<UploadItem> compose = factory.compose(createContext(instrumentAbSCIEX), it1);
        Assert.assertTrue("Error - not found impl AbSciex", Iterables.all(compose, FILE_EXISTS));
        Assert.assertEquals("Error - not properly archived AbSciex", 2, compose.size());

        final ZipFile zipFile = new ZipFile(compose.get(0).getFile());
        final int size = zipFile.size();

        zipFile.close();

        Assert.assertEquals("Error - not all file archived", 3, size);
    }

    //@Test
    public void testDefaultFilter() {
        Context context = createContext(instrumentThermo0);
        File notChangedFile = twoRawFiles1.get(0);
        File changedFile = twoRawFiles1.get(1);
        context.addWaitFile(notChangedFile);
        context.addWaitFile(changedFile);
        for (int i = 0; i < 3; i++) {
            context.incCurrentCountAndUpload();
            context.addWaitFile(changedFile);
        }
        Assert.assertTrue("Error - context get files list", context.getFileList().contains(notChangedFile));
        Assert.assertTrue("Error - context files list is not clear", context.getFileList().isEmpty());
    }

    @Test
    public void testManyFilter() throws IOException {
        Context context = createContext(instrumentAbSCIEX);

        File notChangeFileScan = FileCreator.createFile("test2.wiff.scan");
        File changedFileWiff = FileCreator.createFile("test2.wiff");
        File notChangeFileMtd = FileCreator.createFile("test2.wiff.mtd");
        context.addWaitFile(notChangeFileScan);
        context.incCurrentCountAndUpload();
        context.addWaitFile(notChangeFileMtd);
        context.addWaitFile(changedFileWiff);

        Assert.assertTrue("Error - context contain not all bound files", context.getFileList().size() == 3);

        context.incCurrentCountAndUpload();

        Assert.assertTrue("Error - context after getFilesList not remove files", context.getFileList().isEmpty());
    }
}
*/
