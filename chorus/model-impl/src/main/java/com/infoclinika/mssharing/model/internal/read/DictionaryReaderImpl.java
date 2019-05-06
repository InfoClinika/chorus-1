package com.infoclinika.mssharing.model.internal.read;

import com.infoclinika.mssharing.model.internal.entity.restorable.NgsExperimentType;
import com.infoclinika.mssharing.model.internal.repository.NgsExperimentTypeRepository;
import com.infoclinika.mssharing.model.read.DictionaryReader;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Vitalii Petkanych
 */
@Service
public class DictionaryReaderImpl implements DictionaryReader {

    @Inject
    private NgsExperimentTypeRepository repository;

    @Override
    public List<NgsExperimentTypeDTO> findAllNgsExperimentTypes() {
        return repository
                .findAll()
                .stream()
                .map(t -> new NgsExperimentTypeDTO(t.getId(), t.getTitle(), getTypePrepMethods(t)))
                .collect(Collectors.toList());
    }

    @Override
    public List<ExperimentPrepMethodDTO> findExperimentPrepMethodByNgsExperimentType(int ngsExperimentTypeId) {
        final NgsExperimentType experimentType = repository.findOne(ngsExperimentTypeId);
        return getTypePrepMethods(experimentType);
    }

    private List<ExperimentPrepMethodDTO> getTypePrepMethods(NgsExperimentType type) {
        return type.getPrepMethods()
                .stream()
                .map(p -> new ExperimentPrepMethodDTO(p.getId(), p.getTitle()))
                .collect(Collectors.toList());
    }
}
