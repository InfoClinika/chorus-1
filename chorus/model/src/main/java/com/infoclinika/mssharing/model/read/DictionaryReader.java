package com.infoclinika.mssharing.model.read;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author timofei.kasianov 2/20/18
 */
@Transactional(readOnly = true)
public interface DictionaryReader {

    List<NgsExperimentTypeDTO> findAllNgsExperimentTypes();

    List<ExperimentPrepMethodDTO> findExperimentPrepMethodByNgsExperimentType(int ngsExperimentTypeId);


    class NgsExperimentTypeDTO {
        public final int id;
        public final String title;
        public final List<ExperimentPrepMethodDTO> prepMethods;

        public NgsExperimentTypeDTO(int id, String title, List<ExperimentPrepMethodDTO> prepMethods) {
            this.id = id;
            this.title = title;
            this.prepMethods = prepMethods;
        }
    }

    class ExperimentPrepMethodDTO {
        public final int id;
        public final String title;

        public ExperimentPrepMethodDTO(int id, String title) {
            this.id = id;
            this.title = title;
        }
    }
}
