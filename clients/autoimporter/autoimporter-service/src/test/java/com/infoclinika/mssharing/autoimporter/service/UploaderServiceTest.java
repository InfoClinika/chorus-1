/*
package com.infoclinika.mssharing.autoimporter.service;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.infoclinika.mssharing.autoimporter.model.bean.WaitItem;
import com.infoclinika.mssharing.autoimporter.service.api.internal.UploadService;
import com.infoclinika.mssharing.autoimporter.service.util.AbstractTestSuite;
import com.infoclinika.mssharing.autoimporter.service.util.FileCreator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.testng.AssertJUnit.
import org.junit.Test;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.util.List;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.autoimporter.model.util.UploadTransformer.FILE_TO_STRING;

*/
/**
 * author Ruslan Duboveckij
 *//*

@DependsOn({"ComposerTest", "FileFilterServiceTest", "TaskUploaderTest"})
public class UploaderServiceTest extends AbstractTestSuite {
    private static final Function<WaitItem, File> TO_FILE = new Function<WaitItem, File>() {
        @Nullable
        @Override
        public File apply(@Nullable WaitItem input) {
            return input.getFile();
        }
    };
    @Inject
    UploadService uploadService;

    private static <T> Predicate<T> predicateContainAll(final FluentIterable<T> files,
                                                        final Function<T, String> toString) {
        return predicateContainAll(files, toString, toString);
    }

    private static <T, F> Predicate<F> predicateContainAll(final FluentIterable<T> files,
                                                           final Function<T, String> toStringT,
                                                           final Function<F, String> toStringF) {
        return new Predicate<F>() {
            @Override
            public boolean apply(@Nullable final F f) {
                return any(files, new Predicate<T>() {
                    @Override
                    public boolean apply(@Nullable T t) {
                        return toStringF.apply(f).equalsIgnoreCase(toStringT.apply(t));
                    }
                });
            }
        };
    }

    //TODO: fix test
    @Test
    public void testMonitorWatchFiles() throws Exception {
        */
/*final String folder1 = folderMany1.getAbsolutePath();
        final String folder2 = folderMany2.getAbsolutePath();
        uploadService.addContext("test1", folder1, "", instrumentThermo0.getId(), specie0.getId());
        uploadService.addContext("test2", folder2, "", instrumentThermo1.getId(), specie0.getId());
        uploadService.startWatch(folder1);
        uploadService.startWatch(folder2);

        final List<File> files1 = newArrayList(twoRawFiles1);
        File file1 = FileCreator.createTempFile(folderMany1);
        files1.add(file1);
        final List<File> files2 = newArrayList(twoRawFiles2);
        File file2 = FileCreator.createTempFile(folderMany2);
        files2.add(file2);
        Thread.sleep(configBean.getMonitorInterval());

        final List<File> allFilesList1 = FluentIterable
                .from(session.getContext(folder1).getWaitList())
                .transform(TO_FILE)
                .toList();

        final List<File> allFilesList2 = FluentIterable
                .from(session.getContext(folder2).getWaitList())
                .transform(TO_FILE)
                .toList();

        final boolean condition = any(files1, predicateContainAll(FluentIterable.from(allFilesList1), FILE_TO_STRING))
        && any(files2, predicateContainAll(FluentIterable.from(allFilesList2), FILE_TO_STRING));

        file1.deleteOnExit();
        file2.deleteOnExit();

        Assert.assertTrue("Error - Context isn`t have same files with folder", condition);*//*

    }
}*/
