package com.infoclinika.mssharing.web.downloader;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.infoclinika.analysis.storage.cloud.CloudStorageFactory;
import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.fileserver.model.StoredFile;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Herman Zamula
 */
@Component
public class BillingHistoryDownloadHelper {

    private static final Logger LOG = Logger.getLogger(BillingHistoryDownloadHelper.class);
    public static final int BUFFER_LENGTH = 1024;
    @Inject
    private StoredObjectPaths paths;
    @Inject
    private StorageService<StoredFile> storageService;
    @Inject
    private RuleValidator validator;

    public void download(long userId, long lab, String path, HttpServletResponse response) throws IOException {

        /*checkAccess(validator.isLabHead(userId, lab),
                "Can download billing data. User is not lab head. User " + userId + ", lab " + lab);*/

        Preconditions.checkNotNull(path, "Path is null");

        final CloudStorageService service = CloudStorageFactory.service();

        final List<CloudStorageItemReference> references = service.list(paths.getRawFilesBucket(), path, Optional.<Date>absent());
        if (references.isEmpty()) {
            LOG.error("Billing history reports cannot be downloaded due to its absence.");
            return;
        }

        prepareDownload(response, lab, path);

        final ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());

        references.stream()
                .filter(ref -> ref.getKey().endsWith(".csv"))
                .peek(ref -> putEntry(zos, ref))
                .map(ref -> storageService.get(new NodePath(ref.getKey())))
                .forEach(storedObject -> copyWithRetry(zos, storedObject.getInputStream(), new byte[BUFFER_LENGTH], 0));

        zos.close();
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");
        response.flushBuffer();
    }

    private void prepareDownload(HttpServletResponse response, long lab, String path) {
        String fileName = Joiner.on("").join(lab, path.substring(path.lastIndexOf("/"))) + ".zip";

        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
    }

    private void copyWithRetry(ZipOutputStream zos, InputStream is, byte[] buf, long written) {
        try {
            if (written > 0) {
                LOG.debug("Retrying download from " + written + " byte");
                is.skip(written);
            }
            long writtenBytes = written;
            while (true) {
                int r = 0;
                try {
                    r = is.read(buf);
                } catch (IOException e) {
                    copyWithRetry(zos, is, buf, writtenBytes);
                }
                if (r == -1) {
                    break;
                }
                zos.write(buf, 0, r);
                writtenBytes += r;
            }
            is.close();
            zos.closeEntry();
        } catch (IOException ex) {
            throw Throwables.propagate(ex);
        }
    }

    private void putEntry(ZipOutputStream zos, CloudStorageItemReference ref) {
        try {
            zos.putNextEntry(new ZipEntry(ref.extractFilename()));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
