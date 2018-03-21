package com.infoclinika.mssharing.integration.test.data.file;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.infoclinika.mssharing.integration.test.utils.Strings.getRandomNumberFrom;
import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;
import static org.testng.AssertJUnit.fail;

/**
 * @author Alexander Orlov
 */
public class FileData {
    private Log log = LogFactory.getLog(this.getClass());

    private File parentFolder;
    private File folder;
    private String[] files;

    public FileData(String... files) {
        this.files = files;
    }

    public void createFiles() {
        createTempFolder();
        for (String file : getFiles()) {
            final File targetFile = new File(getFolder(), file);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(targetFile);
                final byte[] bytes = new byte[1024];
                //create file with random size from 1 kb to 2 Mb
                int i;
                for (i = 0; i < getRandomNumberFrom(1000, 2048); i++) {
                    fos.write(bytes);
                }
                fos.flush();
                log.info("- - File '" + file + "' created successfully");
                log.info("File size is: " + i + "kb");
            } catch (IOException e) {
                fail("File creation failed" + e.getMessage());
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }

    }

    public void createFilesWithSizeInMb(int size){
        createTempFolder();
        for (String file : getFiles()) {
            final File targetFile = new File(getFolder(), file);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(targetFile);
                final byte[] bytes = new byte[1024];
                int i;
                for (i = 0; i < size*1024; i++) {
                    fos.write(bytes);
                }
                fos.flush();
                log.info("- - File '" + file + "' created successfully");
                log.info("File size is: " + i + "kb");
            } catch (IOException e) {
                fail("File creation failed" + e.getMessage());
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    public File createTempFolder() {
        parentFolder = new File(System.getProperty("user.home") + File.separator + "TestFiles");
        if (!parentFolder.exists()) {
            parentFolder.mkdir();
        }
        folder = new File(parentFolder + File.separator + randomizeName("F"));
        boolean isCreated = folder.mkdir();
        if (isCreated) {
            log.info("Temporary folder with path " + folder + " created ");
        } else {
            log.warn("Temporary folder with path " + folder + " is not created ");
        }
        return folder;
    }

    public File getFolder() {
        return folder;
    }

    public String[] getFiles() {
        return files;
    }

    public void removeFilesAndFolder() {
        for (String fileName : files) {
            File file = new File(getFolder(), fileName);
            boolean isDeleted = file.delete();
            if (isDeleted) {
                log.info("File with name: " + file.getName() + " has been deleted");
            } else {
                log.warn("File with name: " + file.getName() + " is not deleted");
            }
        }
        boolean isChildFolder = getFolder().delete();
        if (isChildFolder) {
            log.info("Folder with path: " + folder + " has been deleted");
        } else {
            log.warn("Folder with path: " + folder + "is not deleted");
        }
        boolean isParentFolderDeleted = parentFolder.delete();
        if (isParentFolderDeleted) {
            log.info("Folder with path: " + parentFolder + " has been deleted");
        } else {
            log.warn("Folder with path: " + parentFolder + " is not deleted");
        }
    }

    public File createWatersFolder() {
        parentFolder = new File(System.getProperty("user.home") + File.separator + "WatersFolder");
        if (!parentFolder.exists()) {
            parentFolder.mkdir();
        }
        folder = new File(parentFolder + File.separator + randomizeName("Waters") + ".raw");
        boolean isCreated = folder.mkdir();
        if (isCreated) {
            log.info("Temporary folder with path " + folder + " created ");
        } else {
            log.warn("Temporary folder with path " + folder + " is not created ");
        }
        return folder;
    }

    public void createWatersFolderWithSizeInMb(int size){
        createWatersFolder();
        for (String file : getFiles()) {
            final File targetFile = new File(getFolder(), file);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(targetFile);
                final byte[] bytes = new byte[1024];
                int i;
                for (i = 0; i < size*1024; i++) {
                    fos.write(bytes);
                }
                fos.flush();
                log.info("- - File '" + file + "' created successfully");
                log.info("File size is: " + i + "kb");
            } catch (IOException e) {
                fail("File creation failed" + e.getMessage());
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }
}
