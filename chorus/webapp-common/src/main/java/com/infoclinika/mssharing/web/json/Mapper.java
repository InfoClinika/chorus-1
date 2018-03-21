package com.infoclinika.mssharing.web.json;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.infoclinika.mssharing.model.helper.ExperimentPreparedSampleItem;
import com.infoclinika.mssharing.model.helper.ExperimentSampleItem;
import com.infoclinika.mssharing.model.helper.LockMzItem;
import com.infoclinika.mssharing.model.write.*;
import com.infoclinika.mssharing.platform.web.json.MapperTemplate;

import javax.annotation.PostConstruct;

/**
 * @author Pavel Kaplin
 *         <p/>
 *         Motivated by http://wiki.fasterxml.com/JacksonMixInAnnotations
 */
public class Mapper extends MapperTemplate {
    public Mapper() {

        super();

        addMixInAnnotations(ProjectInfo.class, ProjectInfoMixin.class);
        addMixInAnnotations(ExperimentInfo.class, ExperimentInfoMixin.class);
        addMixInAnnotations(FileItem.class, FileItemMixin.class);
        addMixInAnnotations(ExperimentSampleItem.class, ExperimentSampleItemMixin.class);
        addMixInAnnotations(ExperimentPreparedSampleItem.class, ExperimentPreparedSampleItemMixin.class);
        addMixInAnnotations(InstrumentDetails.class, InstrumentDetailsMixin.class);
        addMixInAnnotations(InstrumentManagement.UploadFileItem.class, UploadFileItemMixin.class);
        addMixInAnnotations(LockMzItem.class, LockMzItemMixin.class);

    }
    
    @PostConstruct
    public void customConfiguration() {
        // Uses Enum.toString() for serialization of an Enum
        this.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    }
}
