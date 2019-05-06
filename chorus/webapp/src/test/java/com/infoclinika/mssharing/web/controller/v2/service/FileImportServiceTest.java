package com.infoclinika.mssharing.web.controller.v2.service;

import com.infoclinika.mssharing.model.internal.entity.upload.FileDetails;
import com.infoclinika.util.FilenameUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Vitalii Petkanych
 */
public class FileImportServiceTest {

    private FileImportService cut;

    @Test
    public void testAllowAll() throws Exception {
        final Predicate<String> filter = cut.buildFilter(Arrays.asList(FilenameUtil.ALL_FILES_STAR_DOT_STAR, "*.txt"));
        assertTrue(filter.test(""));
        assertTrue(filter.test("test.txt"));
        assertTrue(filter.test("test.html"));
        assertTrue(filter.test("test.xml"));
        assertTrue(filter.test("test.XML"));
        assertTrue(filter.test("test.XmL"));
    }

    @Test
    public void testFilterTxt() throws Exception {
        final Predicate<String> filter = cut.buildFilter(Collections.singletonList("*.txt"));
        assertFalse(filter.test(""));
        assertTrue(filter.test("test.txt"));
        assertTrue(filter.test("test.TXT"));
        assertTrue(filter.test("test.TxT"));
        assertFalse(filter.test("test.html"));
        assertFalse(filter.test("test.xml"));
    }

    @BeforeMethod
    public void setUp() throws Exception {
        cut = new FileImportService() {
            @Override
            public List<FileDetails> listFiles(
                String user,
                String pass,
                String url,
                boolean recursive,
                Predicate<String> filter
            ) {
                return null;
            }

            @Override
            public void copyFiles(
                String user,
                String pass,
                String url,
                List<String> files,
                Function<String, String> dstKeyGenerator
            ) {

            }
        };
    }
}
