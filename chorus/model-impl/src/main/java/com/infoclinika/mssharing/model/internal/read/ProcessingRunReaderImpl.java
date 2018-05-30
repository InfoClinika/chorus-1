package com.infoclinika.mssharing.model.internal.read;

import com.infoclinika.mssharing.model.internal.repository.ProcessingRunRepository;
import com.infoclinika.mssharing.model.read.ProcessingRunReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service
@Transactional
public class ProcessingRunReaderImpl implements ProcessingRunReader{

    @Inject
    private ProcessingRunRepository processingRunRepository;



    @Override
    public boolean findByProcessingRunName(String name, long experiment) {
        return processingRunRepository.findByNameAndExperiment(name, experiment) != null ? true : false;
    }

}
