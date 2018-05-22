package com.infoclinika.mssharing.model.internal.read;


import com.infoclinika.mssharing.model.internal.entity.ProcessingFile;
import com.infoclinika.mssharing.model.internal.repository.ProcessingFileRepository;
import org.springframework.stereotype.Service;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import java.util.List;

@Service
public class ProcessingFileReaderImpl implements ProcessingFileReader{

    @Inject
    private ProcessingFileRepository processingFileRepository;


    @Override
    public ProcessingFileInfo readProcessingFileInfo(long processingFile) {
        final ProcessingFile processingFile1 = find(processingFile);
        return new ProcessingFileInfo(processingFile1.getName(),
                                        processingFile1.getContentId(),
                                        processingFile1.getFileMetaDataTemplates(),
                                        processingFile1.getProcessingRuns(), processingFile1.getExperimentTemplate());
    }

    @Override
    public List<ProcessingFile> readProcessingFilesByExperiment(long experiment) {
        return processingFileRepository.findProcessingFilesByExperiment(experiment);
    }


    private ProcessingFile find(long id){
        return checkNotNull(processingFileRepository.findOne(id), "Couldn't find processing file with id %s", id);
    }

}
