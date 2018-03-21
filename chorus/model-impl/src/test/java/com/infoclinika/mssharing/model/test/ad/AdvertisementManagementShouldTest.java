//package com.infoclinika.mssharing.model.test.ad;
//
//import com.google.common.collect.ImmutableSet;
//import com.infoclinika.analysis.storage.cloud.CloudStorageFactory;
//import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
//import com.infoclinika.analysis.storage.cloud.CloudStorageService;
//import com.infoclinika.mssharing.model.helper.AbstractTest;
//import com.infoclinika.mssharing.model.read.AdvertisementReader;
//import com.infoclinika.mssharing.model.read.AdvertisementReader.AdvertisementDetailsItem;
//import com.infoclinika.mssharing.model.read.AdvertisementReader.AdvertisementItem;
//import com.infoclinika.mssharing.model.write.AdvertisementManagement;
//import com.infoclinika.mssharing.platform.model.AccessDenied;
//import org.apache.log4j.Logger;
//import org.junit.Assert;
//import org.junit.Rule;
//import org.junit.rules.TemporaryFolder;
//import org.testng.annotations.AfterTest;
//import org.testng.annotations.BeforeTest;
//import org.testng.annotations.Test;
//
//import javax.inject.Inject;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.*;
//
//import static org.testng.Assert.*;
//
///**
// * @author andrii.loboda
// */
//
////TODO: this functionality will be needed in the future
//
//public class AdvertisementManagementShouldTest extends AbstractTest {
//    private static final String TESTS_BUCKET = "chorus-opensource-dist";
//    private static final String ADVERTISEMENT_IMAGES_FOLDER = "advertisement-images";
//    private CloudStorageItemReference tempAdFolderForTest;
//    @Inject
//    private AdvertisementManagement advertisementManagement;
//    @Inject
//    private AdvertisementReader advertisementReader;
//    @Rule
//    public TemporaryFolder temporaryFolder = new TemporaryFolder();
//
//    private static final CloudStorageService CLOUD = CloudStorageFactory.service();
//
//    @BeforeTest
//    public void createFolderForAd(){
//        tempAdFolderForTest = createRandomTempFolder(ADVERTISEMENT_IMAGES_FOLDER);
//    }
//
//    @AfterTest
//    public void removeAllFiles(){
//        CLOUD.deleteFromCloud(tempAdFolderForTest);
//    }
//
//
//    @Test(enabled = false)
//    public void read_advertisement_to_display_if_one_has_expired() {
//        assertEquals(advertisementReader.readAll(admin()).size(), 0);
//        final Date currentDate = currentDate();
//        final long advertisement1 = createTestAd(currentDate, add2Month(currentDate), currentDate);
//        final long advertisement2 = createTestAd(currentDate, add20sec(currentDate), currentDate);
//        assertEquals(advertisementReader.readAll(admin()).size(), 2);
//
//        final AdvertisementItem advertisementItem = advertisementReader.readAdvertisementToDisplay(add20sec(add20sec(currentDate))); // to expire advertisement2
//
//        assertEquals(advertisementItem.id, advertisement1);
//    }
//
//    @Test(enabled = false)
//    public void read_advertisement_to_display_if_two_available() {
//        assertEquals(advertisementReader.readAll(admin()).size(), 0);
//        final Date currentDate = currentDate();
//        final long advertisement1 = createTestAd(currentDate, add2Month(currentDate), currentDate);
//        final long advertisement2 = createTestAd(currentDate, add2Month(currentDate), currentDate);
//        assertEquals(advertisementReader.readAll(admin()).size(), 2);
//
//        final AdvertisementItem advertisementItem = advertisementReader.readAdvertisementToDisplay(currentDate);
//        final ImmutableSet<Long> possibleAds = ImmutableSet.of(advertisement1, advertisement2);
//        assertTrue(possibleAds.contains(advertisementItem.id));
//    }
//
//    @Test(enabled = false)
//    public void read_advertisement_to_display_if_all_have_expired() {
//        assertEquals(advertisementReader.readAll(admin()).size(), 0);
//        final Date currentDate = currentDate();
//        final long advertisement1 = createTestAd(currentDate, add20sec(currentDate), currentDate);
//        final long advertisement2 = createTestAd(currentDate, add20sec(currentDate), currentDate);
//        assertEquals(advertisementReader.readAll(admin()).size(), 2);
//
//        final AdvertisementItem advertisementItem = advertisementReader.readAdvertisementToDisplay(add20sec(add20sec(currentDate))); // to expire all adverts
//
//        assertNull(advertisementItem);
//    }
//
//    @Test(expectedExceptions = AccessDenied.class, enabled = false)
//    public void not_allow_to_read_list_of_advertisement_for_non_admin_users(){
//        final long paul = uc.createPaul();
//        advertisementReader.readAll(paul);
//    }
//
//    @Test(expectedExceptions = AccessDenied.class, enabled = false)
//    public void not_allow_to_create_advertisement_as_non_admin_users(){
//        final long paul = uc.createPaul();
//        final String title = "Pavel's Kaplin Advertisement";
//        final String redirectLink = "https://www.google.com.ua/?gfe_rd=cr&ei=gOsgVuqDL7St8wehnYWQBA&gws_rd=ssl";
//        final Date currentDate = currentDate();
//        advertisementManagement.createAdvertisement(paul, new AdvertisementManagement.AdvertisementInfo(title, currentDate, add2Month(currentDate), redirectLink, currentDate, "image.jpg", 100, true));
//    }
//
//    @Test(expectedExceptions = AccessDenied.class, enabled = false)
//    public void not_allow_to_delete_advertisement_as_non_admin_users(){
//        final long paul = uc.createPaul();
//        final Date currentDate = currentDate();
//        final long advertisement = createTestAd(currentDate, add2Month(currentDate), currentDate);
//        advertisementManagement.deleteAdvertisement(paul, advertisement);
//    }
//
//    @Test(expectedExceptions = AccessDenied.class, enabled = false)
//    public void not_allow_to_read_advertisement_details_for_non_admin_users(){
//        final long paul = uc.createPaul();
//        final Date currentDate = currentDate();
//        long advertisement = createTestAd(currentDate, add2Month(currentDate), currentDate);
//        advertisementReader.readAdvertisement(paul, advertisement);
//    }
//
//    @Test(enabled = false)
//    public void persist_advertisement() {
//        final String title = "Pavel's Kaplin Advertisement";
//        final String redirectLink = "https://www.google.com.ua/?gfe_rd=cr&ei=gOsgVuqDL7St8wehnYWQBA&gws_rd=ssl";
//        final String imageToDisplayRef = new CloudStorageItemReference(TESTS_BUCKET, "ads/pavel-kaplin-ad.jpg").asDelimitedPath();
//        final Date currentDate = currentDate();
//        String imageName = "New_test_image.png";
//        long imageSize = 23002;
//
//        final long advertisement = createAd(admin(), new AdvertisementManagement.AdvertisementInfo(title, currentDate, add2Month(currentDate), redirectLink, currentDate, imageName, imageSize, true));
//        AdvertisementDetailsItem advertisementDetailsItem = advertisementReader.readAdvertisement(admin(), advertisement);
//        List<AdvertisementReader.AdvertisementAttachmentItem> attachmentList = advertisementReader.readAttachment(admin(), advertisement);
//        assertNotNull(advertisementDetailsItem);
//        assertEquals(advertisementDetailsItem.title, title);
//        assertEquals(advertisementDetailsItem.redirectLink, redirectLink);
//        assertEquals(advertisementDetailsItem.clickedCount, 0);
//        assertEquals(advertisementDetailsItem.displayedCount, 0);
//        assertEquals(attachmentList.get(0).name, imageName);
//        assertEquals(attachmentList.get(0).sizeInBytes, imageSize);
//    }
//
//
//    @Test(enabled = false)
//    public void increment_advertisement_click_count() {
//        final Date startDate = currentDate();
//        final Date currentDate = add20sec(startDate);
//        final long advertisement = createTestAd(startDate, add2Month(currentDate), currentDate);
//
//        advertisementManagement.incrementClickedCount(advertisement, currentDate);
//
//        final AdvertisementDetailsItem adChanged = advertisementReader.readAdvertisement(admin(), advertisement);
//        assertNotNull(adChanged);
//        assertEquals(adChanged.displayedCount, 0);
//        assertEquals(adChanged.clickedCount, 1);
//    }
//
//    @Test(enabled = false)
//    public void increment_advertisement_display_count() {
//        final Date startDate = currentDate();
//        final Date currentDate = add20sec(startDate);
//        final long advertisement = createTestAd(startDate, add2Month(currentDate), currentDate);
//
//        advertisementManagement.incrementDisplayedCount(advertisement, currentDate);
//
//        final AdvertisementDetailsItem adChanged = advertisementReader.readAdvertisement(admin(), advertisement);
//        assertNotNull(adChanged);
//        assertEquals(adChanged.displayedCount, 1);
//        assertEquals(adChanged.clickedCount, 0);
//    }
//
//    @Test(expectedExceptions = IllegalStateException.class, enabled = false)
//    public void increment_advertisement_click_count_if_expired() {
//        final Date currentDate = currentDate();
//        final long advertisement = createTestAd(currentDate, add20sec(currentDate), currentDate);
//        advertisementManagement.incrementClickedCount(advertisement, add20sec(add20sec(currentDate))); // to expire
//        final AdvertisementDetailsItem adChanged = advertisementReader.readAdvertisement(admin(), advertisement);
//        assertNotNull(adChanged);
//        assertEquals(adChanged.displayedCount, 0);
//        assertEquals(adChanged.clickedCount, 0);
//    }
//
//    @Test(expectedExceptions = IllegalStateException.class, enabled = false)
//    public void increase_advertisement_displayed_count_if_expired() {
//        final Date currentDate = currentDate();
//        final long advertisement = createTestAd(currentDate, add20sec(currentDate), currentDate);
//        advertisementManagement.incrementDisplayedCount(advertisement, add20sec(add20sec(currentDate))); // to expire
//        final AdvertisementDetailsItem adChanged = advertisementReader.readAdvertisement(admin(), advertisement);
//        assertNotNull(adChanged);
//        assertEquals(adChanged.displayedCount, 0);
//        assertEquals(adChanged.clickedCount, 0);
//    }
//
//
//    @Test(enabled = false)
//    public void delete_advertisement() {
//        final Date currentDate = currentDate();
//        assertNull(advertisementReader.readAdvertisementToDisplay(currentDate));
//        final long advertisement = createTestAd(currentDate, add2Month(currentDate), currentDate);
//        assertNotNull(advertisementReader.readAdvertisementToDisplay(currentDate));
//
//        advertisementManagement.deleteAdvertisement(admin(), advertisement);
//
//        assertNull(advertisementReader.readAdvertisement(admin(), advertisement));
//        assertNull(advertisementReader.readAdvertisementToDisplay(currentDate));
//    }
//
//    private long createTestAd(Date startDate, Date endDate, Date currentDate) {
//        return createAd(admin(), new AdvertisementManagement.AdvertisementInfo("Test Ad", startDate, endDate, "http://chorusproject.org",currentDate, "ad_image.jpg", 23002, true));
//    }
//
//    private long createAd(long userId, AdvertisementManagement.AdvertisementInfo advertisementInfo) {
//        long adId = advertisementManagement.createAdvertisement(userId, new AdvertisementManagement.AdvertisementInfo(
//                advertisementInfo.title,
//                advertisementInfo.startDate,
//                advertisementInfo.endDate,
//                advertisementInfo.redirectLink,
//                advertisementInfo.currentDate,
//                advertisementInfo.imageName,
//                advertisementInfo.imageSize,
//                advertisementInfo.isEnabled));
//        CLOUD.uploadToCloud(getFileContents(), TESTS_BUCKET, tempAdFolderForTest.getKey() + adId);
//        advertisementManagement.specifyAdvertisementContent(admin(), adId, tempAdFolderForTest.getKey() + adId);
//        return adId;
//    }
//
//    private static Date add20sec(Date startDate) {
//        final Calendar endDate = Calendar.getInstance();
//        endDate.setTime(startDate);
//        endDate.add(Calendar.SECOND, 20);
//        return endDate.getTime();
//    }
//
//    private static Date add2Month(Date startDate) {
//        final Calendar endDate = GregorianCalendar.getInstance();
//        endDate.setTime(startDate);
//        endDate.add(Calendar.MONTH, 2);
//        return endDate.getTime();
//    }
//
//    private static Date remove2Month(Date startDate) {
//        final Calendar endDate = GregorianCalendar.getInstance();
//        endDate.setTime(startDate);
//        endDate.add(Calendar.MONTH, -2);
//        return endDate.getTime();
//    }
//
//    private static Date currentDate() {
//        final Calendar endDate = GregorianCalendar.getInstance();
//        endDate.setTime(new Date());
//        endDate.add(Calendar.MONTH, -2);
//        return endDate.getTime();
//    }
//
//    private File getTempFileWithContents(String contents) throws IOException {
//        final File tempFile = temporaryFolder.newFile("" + System.currentTimeMillis());
//        return fillTempFileContents(contents, tempFile);
//    }
//
//    private CloudStorageItemReference createRandomTempFolder(String advertisementRootFolder){
//        final String folderName = UUID.randomUUID().toString();
//        final File tempFolderToUpload = temporaryFolder.newFolder(folderName);
//        final CloudStorageItemReference uploadTarget = new CloudStorageItemReference(TESTS_BUCKET, advertisementRootFolder);
//        return CLOUD.uploadFolderToCloud(tempFolderToUpload, uploadTarget);
//    }
//
//    private File fillTempFileContents(String contents, File tempFile) throws IOException {
//        final FileOutputStream fos = new FileOutputStream(tempFile);
//        final PrintWriter printWriter = new PrintWriter(fos);
//        printWriter.println(contents);
//        fos.flush();
//        printWriter.close();
//        fos.close();
//        return tempFile;
//    }
//
//    private File getFileContents(){
//        final StringBuilder contentsBuilder = new StringBuilder();
//        final String literalPhrase = "This is a test file";
//        final int totalParts = 1000;
//        final int totalDecimalSigns = ("" + totalParts).length();
//        for(int i = 0; i < totalParts; i++) {
//            contentsBuilder.append(literalPhrase);
//
//            for(int k = 0; k < (totalDecimalSigns - ("" + i).length()); k++) {
//                contentsBuilder.append(0);
//            }
//            contentsBuilder.append(i);
//        }
//        final String contents = contentsBuilder.toString();
//        System.out.println("Total contents size: " + contents.length());
//        File tempFile = null;
//        try {
//            tempFile = getTempFileWithContents(contents);
//        } catch (IOException e) {
//            e.printStackTrace();
//            Assert.fail("Exception: " + e.getMessage());
//        }
//        return tempFile;
//    }
//}
