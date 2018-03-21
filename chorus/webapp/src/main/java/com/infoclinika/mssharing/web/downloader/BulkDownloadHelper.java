package com.infoclinika.mssharing.web.downloader;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.fileserver.model.ArchivedFile;
import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.helper.items.ChorusExperimentDownloadData;
import com.infoclinika.mssharing.model.helper.items.ChorusFileData;
import com.infoclinika.mssharing.model.write.FileMovingManager;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.web.downloader.BulkDownloadHelperTemplate;
import com.infoclinika.mssharing.services.billing.rest.api.BillingService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.FilterInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.infoclinika.mssharing.model.helper.ExperimentDownloadHelper.AttachmentDataTemplate;
import static com.infoclinika.mssharing.model.helper.ExperimentDownloadHelper.ExperimentItemTemplate;

/**
 * @author Alexei Tymchenko
 */
@Service
public class BulkDownloadHelper extends BulkDownloadHelperTemplate<ExperimentItemTemplate, ChorusExperimentDownloadData, ChorusFileData> {

    @Inject
    private StorageService<ArchivedFile> archiveStorageService;

    @Inject
    private FileMovingManager fileMovingManager;

    @Resource(name = "billingRestService")
    private BillingService billingService;

    @Inject
    private SecurityHelper securityHelper;


    @Override
    protected  void beforeDownloadFiles(long userId, Set<Long> fileIds, List<ChorusFileData> request) {
        fileMovingManager.updateAccessForFile(fileIds);
    }
    
    @Override
    protected  void beforeDownloadExperiment(long userId, long experimentId, ChorusExperimentDownloadData request) {
        super.beforeDownloadExperiment(userId, experimentId, request);
        fileMovingManager.updateAccessForExperiment(experimentId);
    }

    @Override
    protected  void writeExperimentHeader(BufferedWriter bw, ChorusExperimentDownloadData experiment) throws IOException {
        super.writeExperimentHeader(bw, experiment);
        final String lockMasses = Joiner.on(", ").join(experiment.lockMzs);
        writeHeaderLine(bw, "Lock m/z", lockMasses);
    }


    @Override
    protected <ATTACHMENT_ITEM extends AttachmentDataTemplate> BulkDownloadHelperTemplate.InputStreamProvider getAttachmentsStreamProvider(ATTACHMENT_ITEM item) {
        return super.getAttachmentsStreamProvider(item);
    }

    @Override
    protected BulkDownloadHelperTemplate.InputStreamProvider getFileStreamProvider(final ChorusFileData file) {
        if(file.archiveId == null) {
            return super.getFileStreamProvider(file);
        }
        return new InputStreamProvider() {
            @Override
            public FilterInputStream get() {
                return archiveStorageService.get(new NodePath(file.archiveId)).getInputStream();
            }
        };
    }

    private Predicate<ChorusFileData> isFileInUserLab(final Set<Long> labs) {
        return new Predicate<ChorusFileData>() {
            @Override
            public boolean apply(ChorusFileData file) {
                return labs.contains(file.billLab.or(file.lab));
            }
        };
    }

    private void billFilesDownload(long userId, List<ChorusFileData> files, Long billingLab, boolean anonymous, boolean experimentDownload) {
        final ImmutableSet<Long> userLabs = securityHelper.getUserDetails(userId).labs;
        for (ChorusFileData file : files) {
            final Long labToBill = !isFileInUserLab(userLabs, file) || experimentDownload ? billingLab : file.billLab.or(file.lab);
            switch (file.accessLevel) {
                case SHARED:
                case PRIVATE:
                    billingService.logDownloadUsage(userId, file.id, labToBill);
                    break;
                case PUBLIC:
                    billingService.logPublicDownload(anonymous ? null : userId, file.id);
                    break;
            }
        }
    }

    private boolean isFileInUserLab(ImmutableSet<Long> userLabs, ChorusFileData file) {
        return userLabs.contains(file.billLab.or(file.lab));
    }
    
    public static class ChorusRequest extends Request {

        public final boolean anonymous;
        public final Long lab;

        public ChorusRequest(long actor, Set<Long> fileIds, Long experimentId, boolean anonymous, Long lab) {
            super(actor, fileIds, experimentId);
            this.anonymous = anonymous;
            this.lab = lab;
        }
    }

}
