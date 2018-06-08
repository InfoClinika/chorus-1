package com.infoclinika.mssharing.model.internal.read;


import com.infoclinika.mssharing.model.internal.entity.ProcessingFile;
import com.infoclinika.mssharing.model.internal.repository.ProcessingFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProcessingFileReaderImpl implements ProcessingFileReader {

    @Inject
    private ProcessingFileRepository processingFileRepository;


    @Override
    public ProcessingFileInfo readProcessingFileInfo(long processingFileId) {
        final ProcessingFile processingFile = find(processingFileId);
        return createProcessingFileInfo(processingFile);
    }


    private ProcessingFile find(long id){
        return checkNotNull(processingFileRepository.findOne(id), "Couldn't find processing file with id %s", id);
    }



    // not valid realization
    private ProcessingFileInfo createProcessingFileInfo(ProcessingFile processingFile){
        return new ProcessingFileInfo(processingFile.getName(),
                processingFile.getContentId(), processingFile.getFileMetaDataTemplates(),
                processingFile.getProcessingRuns(), processingFile.getExperiment());
    }
}
