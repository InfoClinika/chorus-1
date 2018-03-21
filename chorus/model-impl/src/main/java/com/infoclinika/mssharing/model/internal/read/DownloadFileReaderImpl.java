package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.model.internal.entity.FilesDownloadGroup;
import com.infoclinika.mssharing.model.internal.entity.FileDownloadJob;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.Util;
import com.infoclinika.mssharing.model.internal.repository.FileDownloadGroupRepository;
import com.infoclinika.mssharing.model.internal.repository.FileDownloadJobRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.read.DownloadFileReader;
import com.infoclinika.mssharing.platform.entity.EntityUtil;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;

import static com.google.common.collect.Sets.newHashSet;

/**
 * TODO <herman.zamula>: This service used only in tests. Do we need this really?
 *
 * @author Elena Kurilina
 */
@Service
public class DownloadFileReaderImpl implements DownloadFileReader {

    @Inject
    private FileDownloadJobRepository fileDownloadJobRepository;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    private FileDownloadGroupRepository fileDownloadGroupRepository;


    @Override
    public DownloadFileJob readJobByFile(Long id) {
        final FileDownloadJob job = fileDownloadJobRepository.findByMetaData(fileMetaDataRepository.findOne(id));
        if (job != null) {
            return new DownloadFileJob(job.getId(), job.isCompleted(), job.fileMetaData.getId());
        } else {
            return null;
        }
    }

    @Override
    public FileItemLocation readFileLocation(Long id) {
        final ActiveFileMetaData file = fileMetaDataRepository.findOne(id);
        return new FileItemLocation(file.getContentId(), file.getArchiveId(), file.getLastAccess());
    }

    @Override
    public ImmutableList<DownloadFileGroup> readGroupByJob(Long id) {
        final Collection<FilesDownloadGroup> group = fileDownloadGroupRepository.findByJob(newHashSet(fileDownloadJobRepository.findOne(id)));
        return ImmutableList.copyOf(Collections2.transform(group, input -> new DownloadFileGroup(input.experimentId,
                Collections2.transform(input.getNotifiers(), EntityUtil.ENTITY_TO_ID),
                Collections2.transform(input.getJobs(), Util.ENTITY_TO_ID)
                )));
    }
}
