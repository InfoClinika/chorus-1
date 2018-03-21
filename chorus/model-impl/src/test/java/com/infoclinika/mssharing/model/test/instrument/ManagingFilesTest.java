package com.infoclinika.mssharing.model.test.instrument;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.infoclinika.analysis.storage.cloud.CloudStorageFactory;
import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.model.write.FileMetaDataInfo;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.read.Filter;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.TemporaryFolder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Iterables.getLast;
import static com.infoclinika.mssharing.model.read.DashboardReader.StorageStatus.ARCHIVED;
import static com.infoclinika.mssharing.model.read.DashboardReader.StorageStatus.UNARCHIVED;
import static com.infoclinika.mssharing.platform.model.read.Filter.ALL;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Herman Zamula
 */
public class ManagingFilesTest extends AbstractInstrumentTest {

    private static final Logger LOGGER = Logger.getLogger(ManagingFilesTest.class);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static final String BUCKET_NAME = "";

    private static final String CONTENT_ID_FILE = "c15092005_000S3.txt";

    private static final CloudStorageService CLOUD_STORAGE_SERVICE = CloudStorageFactory.service();

    private CloudStorageItemReference cloudStorageItemReference = new CloudStorageItemReference(BUCKET_NAME, CONTENT_ID_FILE);

    private Function<FileLine, Long> fileLineIdTransformer = new Function<FileLine, Long>() {
        @Override
        public Long apply(FileLine input) {
            return input.id;
        }
    };

    @BeforeClass
    private void uploadFileToS3Bucket() throws IOException {
        if(!CLOUD_STORAGE_SERVICE.existsAtCloud(cloudStorageItemReference)){
            File file = getFileWithContents("test content");
            CLOUD_STORAGE_SERVICE.uploadToCloud(file, cloudStorageItemReference.getBucket(), cloudStorageItemReference.getKey());
            LOGGER.info("FILE WAS UPLOADED TO STORAGE");
        }
    }



    @AfterClass
    private void removeFileFromS3Bucket(){
        if(CLOUD_STORAGE_SERVICE.existsAtCloud(cloudStorageItemReference)){
            CLOUD_STORAGE_SERVICE.deleteFromCloud(cloudStorageItemReference);
            LOGGER.info("FILE IS DELETED"+ CONTENT_ID_FILE);
        }
    }

    @Test
    void testUserCanSetSpeciesInBulk() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();

        uc.saveFile(bob, instrument);
        uc.saveFile(bob, instrument);

        final Set<FileLine> fileLines = fileReader.readFiles(bob, ALL);
        final Long oldSpecie = fileLines.iterator().next().specieId;

        final ImmutableSet<Long> files = from(fileLines)
                .transform(fileLineIdTransformer)
                .toSet();

        final long newSpecie = anotherSpecie(oldSpecie);

        instrumentManagement.bulkSetSpecies(bob, files, newSpecie);
        final Set<FileLine> updated = fileReader.readFiles(bob, ALL);

        assertThat("New species have not been set correctly", updated, JUnitMatchers.everyItem(matchSpecie(newSpecie)));

    }

    @Test
    void testCanArchiveFiles() throws ExecutionException, InterruptedException {
        setBilling(true);
        final long bob = uc.createLab3AndBob();
        billingManagement.makeLabAccountEnterprise(uc.createPaul(), uc.getLab3());
        final com.infoclinika.mssharing.model.write.FileItem fileItem = anyFile(bob).get(0);
        fileOperationsManager.markFilesToArchive(bob, of(fileItem.id));
        fileOperationsManager.archiveMarkedFiles();
        final FileItem fileDetails = detailsReader.readFile(bob, fileItem.id);
        assertThat(fileDetails.storageStatus, is(DashboardReader.StorageStatus.ARCHIVED));
    }

    @Test
    void testCanUnarchiveFiles() throws ExecutionException, InterruptedException {
        setBilling(true);
        final long bob = uc.createLab3AndBob();
        final com.infoclinika.mssharing.model.write.FileItem fileItem = anyFile(bob).get(0);
        fileOperationsManager.markFilesToArchive(bob, of(fileItem.id));
        fileOperationsManager.archiveMarkedFiles();
        fileOperationsManager.markFilesToUnarchive(bob, of(fileItem.id));
        fileOperationsManager.unarchiveMarkedFiles();
        final FileItem fileDetails = detailsReader.readFile(bob, fileItem.id);
        assertThat(fileDetails.storageStatus, is(UNARCHIVED));
    }

    @Test
    public void testCanArchiveFilesInExperiment() {

        setBilling(true);
        final long bob = uc.createLab3AndBob();
        billingManagement.makeLabAccountEnterprise(uc.createPaul(), uc.getLab3());
        final com.infoclinika.mssharing.model.write.FileItem file = getLast(anyFile(bob));
        createExperiment(bob, createPublicProject(bob), file.id, uc.getLab3());
        fileOperationsManager.markFilesToArchive(bob, of(file.id));
        fileOperationsManager.archiveMarkedFiles();

        final FileItem fileDetails = detailsReader.readFile(bob, file.id);
        assertThat(fileDetails.storageStatus, is(ARCHIVED));
    }

    @Test
    public void testCanArchiveExperimentWithTheFilesPresentInOtherExperiment() {

        setBilling(true);
        final long bob = uc.createLab3AndBob();
        billingManagement.makeLabAccountEnterprise(uc.createPaul(), uc.getLab3());
        final com.infoclinika.mssharing.model.write.FileItem file = getLast(anyFile(bob));
        final long project = createPublicProject(bob);

        final long firstExperiment = createExperiment(bob, project, file.id, uc.getLab3());
        createExperiment(bob, project, file.id, uc.getLab3());

        fileOperationsManager.markExperimentFilesToArchive(bob, firstExperiment);
    }

    @Test
    public void testCanUserEditCopiedThrowProjectFiles() {
        final long bob = uc.createLab3AndBob();
        final long kate = uc.createKateAndLab2();

        //First create project and experiment with one file (by default)
        final long project = uc.createProject(bob);
        createExperiment(bob, project, uc.getLab3());

        studyManagement.newProjectCopyRequest(bob, kate, project);
        studyManagement.approveCopyProjectRequest(kate, project, uc.getLab2());

        final Set<FileLine> kateFiles = dashboardReader.readFiles(kate, ALL);
        assertThat("Kate must has only one file in this case", kateFiles.size(), is(1));

        final FileLine kateFile = kateFiles.iterator().next();
        instrumentManagement.bulkSetLabels(kate, ImmutableSet.of(kateFile.id), "new labels", false);
        final FileLine editedKateFile = dashboardReader.readFiles(kate, ALL).iterator().next();

        assertThat(editedKateFile.columns.labels, is("new labels"));

    }

    @Test
    public void test_check_file_size_consistent(){
        final long bob = uc.createLab3AndBob();
        setFeaturePerLab(ApplicationFeature.TRANSLATION, Lists.newArrayList(uc.getLab3()));
        final Optional<Long> instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3());

        final long fileSize = CLOUD_STORAGE_SERVICE.readContentLength(cloudStorageItemReference);
        final long file = instrumentManagement.startUploadFile(bob, instrument.get(), new FileMetaDataInfo(UUID.randomUUID().toString(), fileSize, "", null, unspecified(), false));
        instrumentManagement.completeMultipartUpload(bob, file, CONTENT_ID_FILE);

        final FileLine fileBeforeCheck = fileReader.readFiles(bob, Filter.ALL).iterator().next();
        Assert.assertTrue("Error. File size is not consistent.", fileBeforeCheck.sizeIsConsistent);

        fileOperationsManager.checkIsFilesConsistent(admin());

        final FileLine checkedFile = fileReader.readFiles(bob, Filter.ALL).iterator().next();
        Assert.assertTrue("Error. File size is not consistent.", checkedFile.sizeIsConsistent);
    }

    private TypeSafeMatcher<FileLine> matchSpecie(final long newSpecie) {
        return new TypeSafeMatcher<FileLine>() {
            @Override
            public boolean matchesSafely(FileLine item) {
                return item.specieId == newSpecie;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(" specie id is new: " + newSpecie);
            }
        };
    }

    private long anotherSpecie(final long oldSpecie) {
        return from(experimentCreationHelper.species()).firstMatch(new Predicate<DictionaryItem>() {
            @Override
            public boolean apply(DictionaryItem input) {
                return input.id != oldSpecie;
            }
        }).get().id;
    }

    private File getFileWithContents(String contents) throws IOException {
        final File file = temporaryFolder.newFile("" + CONTENT_ID_FILE);
        return fillFileContents(contents, file);
    }

    private File fillFileContents(String contents, File tempFile) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        OutputStreamWriter outputStreamWriter =  new OutputStreamWriter(fileOutputStream);
        Writer writer = new BufferedWriter(outputStreamWriter);
        writer.write(contents);
        writer.close();
        fileOutputStream.close();
        return tempFile;
    }
}
