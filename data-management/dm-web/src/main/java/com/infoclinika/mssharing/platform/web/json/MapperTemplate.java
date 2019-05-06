package com.infoclinika.mssharing.platform.web.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.InstrumentManagementTemplate;

/**
 * @author Pavel Kaplin
 *     <p/>
 *     Motivated by http://wiki.fasterxml.com/JacksonMixInAnnotations
 */
public class MapperTemplate extends ObjectMapper {

    public MapperTemplate() {
        addMixIn(ExperimentManagementTemplate.Restriction.class, RestrictionMixin.class);
        addMixIn(ExperimentManagementTemplate.MetaFactorTemplate.class, MetaFactorMixin.class);
        addMixIn(ExperimentManagementTemplate.AnnotationTemplate.class, AnnotationMixin.class);
        addMixIn(ExperimentManagementTemplate.FileItemTemplate.class, ExperimentFileItemMixin.class);
        addMixIn(InstrumentManagementTemplate.InstrumentDetailsTemplate.class, InstrumentDetailsTemplateMixin.class);

        registerModule(new GuavaModule());
    }
}
