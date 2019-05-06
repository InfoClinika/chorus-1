package com.infoclinika.mssharing.web.json;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.infoclinika.mssharing.model.helper.ExperimentPreparedSampleItem;
import com.infoclinika.mssharing.model.helper.ExperimentSampleItem;
import com.infoclinika.mssharing.model.helper.LockMzItem;
import com.infoclinika.mssharing.model.write.ExperimentInfo;
import com.infoclinika.mssharing.model.write.FileItem;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.model.write.ProjectInfo;
import com.infoclinika.mssharing.platform.web.json.MapperTemplate;

import javax.annotation.PostConstruct;

/**
 * @author Pavel Kaplin
 *     <p/>
 *     Motivated by http://wiki.fasterxml.com/JacksonMixInAnnotations
 */
public class Mapper extends MapperTemplate {

    public Mapper() {
        super();

        addMixIn(ProjectInfo.class, ProjectInfoMixin.class);
        addMixIn(ExperimentInfo.class, ExperimentInfoMixin.class);
        addMixIn(FileItem.class, FileItemMixin.class);
        addMixIn(ExperimentSampleItem.class, ExperimentSampleItemMixin.class);
        addMixIn(ExperimentPreparedSampleItem.class, ExperimentPreparedSampleItemMixin.class);
        addMixIn(InstrumentManagement.UploadFileItem.class, UploadFileItemMixin.class);
        addMixIn(LockMzItem.class, LockMzItemMixin.class);

    }

    @PostConstruct
    public void customConfiguration() {
        // Uses Enum.toString() for serialization of an Enum
        this.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    }
}
