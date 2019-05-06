package com.infoclinika.mssharing.platform.web.downloader;

import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.fileserver.model.StoredFile;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate.AttachmentDataTemplate;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate.ExperimentDownloadDataTemplate;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate.FileDataTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.collect.Lists.newLinkedList;
import static com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate.ConditionDataTemplate;
import static com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate.ExperimentDownloadDataTemplate.EXPERIMENT_NAME;
import static com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate.ExperimentItemTemplate;
import static org.apache.commons.fileupload.util.Streams.copy;

/**
 * @author Alexei Tymchenko
 */
@Service
public class BulkDownloadHelperTemplate<
    EXPERIMENT_ITEM extends ExperimentItemTemplate,
    EXPERIMENT_DOWNLOAD_DATA extends ExperimentDownloadDataTemplate,
    FILE_DATA extends FileDataTemplate> {

    private static final String EXPERIMENT_CSV_FILE_NAME = "experiment.csv";
    private static final String CSV_COLUMN_DELIMITER = ", ";
    private static final String CSV_END_LINE_SYMBOL = "\r\n";
    private static final String NO_CONDITION_PLACEHOLDER = "<No condition>";
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkDownloadHelperTemplate.class);
    private static final int BUF_SIZE = 0x10000; // 64K
    private static final String ZIP_ENTRY_FOLDER_SEPARATOR = "/";

    @Inject
    private StorageService fileStorageService;

    @Inject
    private ExperimentDownloadHelperTemplate<EXPERIMENT_ITEM, EXPERIMENT_DOWNLOAD_DATA, FILE_DATA>
        experimentDownloadHelper;

    private static ConditionDataTemplate getConditionOfFileByExperiment(FileDataTemplate file, String experimentName) {
        for (ConditionDataTemplate item : file.conditions) {
            if (item.experimentName.equals(experimentName)) {
                return item;
            }
        }

        return null;
    }

    public void download(final Request request, HttpServletResponse response) throws IOException {
        if (request.experimentId != null) {
            handleDownloadExperiment(request.actor, request.experimentId, response);
        } else {
            handleDownloadFiles(request.actor, request.fileIds, response);
        }
    }

    protected void beforeDownloadExperiment(long userId, long experimentId, EXPERIMENT_DOWNLOAD_DATA request) {
    }

    protected void beforeDownloadFiles(long userId, Set<Long> fileIds, List<FILE_DATA> request) {
    }

    private void handleDownloadExperiment(long userId,
                                          long experimentId,
                                          HttpServletResponse response) throws IOException {
        final EXPERIMENT_DOWNLOAD_DATA experimentDownloadData;

        try {
            experimentDownloadData = experimentDownloadHelper.readExperimentDownloadData(userId, experimentId);
        } catch (AccessDenied e) {
            throw new DownloadExperimentDeniedException(e);
        }

        final ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());

        beforeDownloadExperiment(userId, experimentId, experimentDownloadData);
        prepareDownload(response, experimentDownloadData.getFieldValue(EXPERIMENT_NAME));
        putExperimentCSVFile(experimentDownloadData, zos);
        putExperimentAttachments(experimentDownloadData, zos);
        putFiles(experimentDownloadData.files, zos);

        finishDownload(zos, response);
    }

    private void putExperimentCSVFile(EXPERIMENT_DOWNLOAD_DATA experiment, ZipOutputStream stream) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os))) {
            writeExperimentHeader(bw, experiment);
            writeAttachmentsToCSVFile(bw, experiment);

            bw.write(CSV_END_LINE_SYMBOL);
            bw.write(CSV_END_LINE_SYMBOL);
            bw.write(CSV_END_LINE_SYMBOL);

            writeExperimentDesignTableToCSVFile(experiment, bw);

            bw.flush();

            stream.putNextEntry(new ZipEntry(EXPERIMENT_CSV_FILE_NAME));

            try (ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray())) {
                copy(is, stream, false);
            }

            stream.closeEntry();
        }
    }

    protected void writeExperimentHeader(BufferedWriter bw, EXPERIMENT_DOWNLOAD_DATA experiment) throws IOException {
        for (Map.Entry<String, String> e : experiment.getEntries()) {
            writeHeaderLine(bw, e.getKey(), e.getValue());
        }
    }

    protected void writeAttachmentsToCSVFile(BufferedWriter bw,
                                             EXPERIMENT_DOWNLOAD_DATA experiment) throws IOException {

        if (experiment.attachments.isEmpty()) {
            return;
        }

        bw.write(CSV_END_LINE_SYMBOL);
        bw.write(CSV_END_LINE_SYMBOL);
        bw.write(CSV_END_LINE_SYMBOL);
        bw.write("Attachments");
        bw.write(CSV_END_LINE_SYMBOL);
        bw.write(CSV_END_LINE_SYMBOL);

        for (AttachmentDataTemplate item : experiment.attachments) {
            bw.write(item.name);
            bw.write(CSV_END_LINE_SYMBOL);
        }
    }

    private void putExperimentAttachments(final ExperimentDownloadDataTemplate experiment,
                                          ZipOutputStream stream) throws IOException {
        for (final AttachmentDataTemplate item : experiment.attachments) {
            pushFileToZip(stream, item.name, getAttachmentsStreamProvider(item));
        }
    }

    protected <ATTACHMENT_ITEM extends AttachmentDataTemplate> InputStreamProvider getAttachmentsStreamProvider(
        ATTACHMENT_ITEM item
    ) {
        return () -> ((StoredFile) fileStorageService.get(new NodePath(item.contentId))).getInputStream();
    }

    private void putFiles(List<? extends FileDataTemplate> files, ZipOutputStream stream) throws IOException {
        for (final FileDataTemplate file : files) {
            pushFileToZip(
                stream,
                file.instrumentName + ZIP_ENTRY_FOLDER_SEPARATOR + file.name,
                getFileStreamProvider((FILE_DATA) file)
            );
        }
    }

    protected InputStreamProvider getFileStreamProvider(final FILE_DATA file) {
        return () -> ((StoredFile) fileStorageService.get(new NodePath(file.bucket, file.contentId))).getInputStream();
    }

    private void handleDownloadFiles(long userId, Set<Long> fileIds, HttpServletResponse response) throws IOException {
        final List<FILE_DATA> filesData = experimentDownloadHelper.readFilesDownloadData(userId, fileIds);
        final ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
        beforeDownloadFiles(userId, fileIds, filesData);
        prepareDownload(response, null);
        putFiles(filesData, zos);
        finishDownload(zos, response);
    }

    private void finishDownload(ZipOutputStream stream, HttpServletResponse response) throws IOException {
        stream.close();
        //Set cookie to satisfy AJAX downloader at the client:
        //http://johnculviner.com/post/2012/03/22/Ajax-like-feature-rich-file-downloads-with-jQuery-File-Download.aspx
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");
        response.flushBuffer();
    }

    private void prepareDownload(HttpServletResponse response, String experimentName) {
        final String fileNamePrefix = experimentName == null ? "raw-files" : experimentName;
        final String fileName = (fileNamePrefix + '-' + System.currentTimeMillis() + ".zip")
            .replaceAll("[,; ]", "_");

        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
    }

    private void pushFileToZip(ZipOutputStream zos, String name, InputStreamProvider is) throws IOException {
        LOGGER.debug("Putting the next entry to the ZIP: {}", name);
        zos.putNextEntry(new ZipEntry(name));
        byte[] buf = new byte[BUF_SIZE];
        long written = 0;
        copyWithRetry(zos, is, buf, written);
        LOGGER.debug("Closing the entry of ZIP: {}", name);
    }

    private void copyWithRetry(
        ZipOutputStream zos,
        InputStreamProvider isp,
        byte[] buf,
        long written
    ) throws IOException {
        try (InputStream is = isp.get()) {
            if (written > 0) {
                LOGGER.debug("Retrying download from {} byte", written);
                is.skip(written);
            }
            while (true) {
                int r = 0;
                try {
                    r = is.read(buf);
                } catch (IOException e) {
                    copyWithRetry(zos, isp, buf, written);
                }
                if (r == -1) {
                    break;
                }
                zos.write(buf, 0, r);
                written += r;
            }

            zos.closeEntry();
        }
    }

    protected void writeHeaderLine(BufferedWriter bw, String key, String value) throws IOException {
        bw.write(key);
        bw.write(", ");
        if (value == null) {
            bw.write("Unspecified");
        } else {
            bw.write(value);
        }
        bw.write(CSV_END_LINE_SYMBOL);
    }

    private void writeExperimentDesignTableToCSVFile(final EXPERIMENT_DOWNLOAD_DATA experiment,
                                                     BufferedWriter bw) throws IOException {
        if (experiment.files.isEmpty()) {
            return;
        }

        writeExperimentFilesLineHeader(bw);

        final LinkedList<FileDataTemplate> sortedFiles = newLinkedList(experiment.files);

        sortedFiles.sort((o1, o2) -> {
            final ConditionDataTemplate condition1 = getConditionOfFileByExperiment(o1, experiment.getExperimentName());
            final ConditionDataTemplate condition2 = getConditionOfFileByExperiment(o2, experiment.getExperimentName());

            //support files without factors
            if (condition1 == null) {
                return -1;
            }

            if (condition2 == null) {
                return 1;
            }

            return condition1.name.compareTo(condition2.name);
        });

        for (FileDataTemplate file : sortedFiles) {
            writeExperimentFileLine(bw, (FILE_DATA) file, experiment);
        }

        bw.write(CSV_END_LINE_SYMBOL);
    }

    protected void writeExperimentFilesLineHeader(BufferedWriter bw) throws IOException {
        bw.write("Experiment Design");
        bw.write(CSV_END_LINE_SYMBOL);
        //write body  information
        //files table header
        bw.write("File");
        bw.write(CSV_COLUMN_DELIMITER);
        bw.write("Condition");
        bw.write(CSV_END_LINE_SYMBOL);
    }

    protected void writeExperimentFileLine(BufferedWriter bw, FILE_DATA file, EXPERIMENT_DOWNLOAD_DATA experiment)
        throws IOException {
        bw.write("\"" + file.name + "\"");
        bw.write(CSV_COLUMN_DELIMITER);

        final ConditionDataTemplate condition = getConditionOfFileByExperiment(file, experiment.getExperimentName());
        final String conditionString = (condition == null) ? NO_CONDITION_PLACEHOLDER : ("\"" + condition.name + "\"");

        bw.write(conditionString);
        bw.write(CSV_END_LINE_SYMBOL);
    }

    protected interface InputStreamProvider {
        InputStream get();
    }

    public static class Request {
        final long actor;
        final Set<Long> fileIds;
        final Long experimentId;

        public Request(long actor, Set<Long> fileIds, Long experimentId) {
            this.actor = actor;
            this.fileIds = fileIds;
            this.experimentId = experimentId;
        }
    }
}
