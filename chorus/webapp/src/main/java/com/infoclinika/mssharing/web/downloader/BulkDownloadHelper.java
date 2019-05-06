package com.infoclinika.mssharing.web.downloader;

import com.infoclinika.mssharing.fileserver.model.ArchivedFile;
import com.infoclinika.mssharing.model.helper.FeaturesHelper;
import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.helper.items.ChorusExperimentDownloadData;
import com.infoclinika.mssharing.model.helper.items.ChorusFileData;
import com.infoclinika.mssharing.model.write.FileMovingManager;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate.AttachmentDataTemplate;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate.ExperimentItemTemplate;
import com.infoclinika.mssharing.platform.web.downloader.BulkDownloadHelperTemplate;
import com.infoclinika.mssharing.services.billing.rest.api.BillingService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * @author Alexei Tymchenko
 */
@Service
public class BulkDownloadHelper
    extends BulkDownloadHelperTemplate<ExperimentItemTemplate, ChorusExperimentDownloadData, ChorusFileData> {

    @Inject
    private StorageService<ArchivedFile> archiveStorageService;

    @Inject
    private FileMovingManager fileMovingManager;

    @Resource(name = "billingService")
    private BillingService billingService;

    @Inject
    private SecurityHelper securityHelper;
    @Inject
    private FeaturesHelper featuresHelper;

    @Override
    protected void beforeDownloadExperiment(long userId, long experimentId, ChorusExperimentDownloadData request) {
        super.beforeDownloadExperiment(userId, experimentId, request);
        fileMovingManager.updateAccessForExperiment(experimentId);
    }

    @Override
    protected void beforeDownloadFiles(long userId, Set<Long> fileIds, List<ChorusFileData> request) {
        fileMovingManager.updateAccessForFile(fileIds);
    }

    @Override
    protected <ATTACHMENT_ITEM extends AttachmentDataTemplate> InputStreamProvider getAttachmentsStreamProvider(
        ATTACHMENT_ITEM item
    ) {
        return super.getAttachmentsStreamProvider(item);
    }

    @Override
    protected InputStreamProvider getFileStreamProvider(final ChorusFileData file) {
        if (file.archiveId == null) {
            return super.getFileStreamProvider(file);
        }
        return () -> archiveStorageService.get(new NodePath(file.archiveId)).getInputStream();
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
