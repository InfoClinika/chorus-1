import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.fileserver.model.StoredFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.*;

import static org.testng.Assert.*;

/**
 * @author Herman Zamula
 */
@ContextConfiguration(locations = "classpath:dm-fileserver.test.cfg.xml")
public class InMemoryStorageServiceTest extends AbstractTestNGSpringContextTests {

    @Inject
    private StorageService<StoredFile> storageService;

    public InMemoryStorageServiceTest() {
    }

    @Test
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
