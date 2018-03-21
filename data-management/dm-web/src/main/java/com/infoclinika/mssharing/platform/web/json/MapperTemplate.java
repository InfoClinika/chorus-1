package com.infoclinika.mssharing.platform.web.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.InstrumentManagementTemplate;

/**
 * @author Pavel Kaplin
 *         <p/>
 *         Motivated by http://wiki.fasterxml.com/JacksonMixInAnnotations
 */
public class MapperTemplate extends ObjectMapper {
    public MapperTemplate() {
        addMixInAnnotations(ExperimentManagementTemplate.Restriction.class, RestrictionMixin.class);
        addMixInAnnotations(ExperimentManagementTemplate.MetaFactorTemplate.class, MetaFactorMixin.class);
        addMixInAnnotations(ExperimentManagementTemplate.AnnotationTemplate.class, AnnotationMixin.class);
        addMixInAnnotations(ExperimentManagementTemplate.FileItemTemplate.class, ExperimentFileItemMixin.class);
        addMixInAnnotations(InstrumentManagementTemplate.InstrumentDetailsTemplate.class, InstrumentDetailsTemplateMixin.class);
        registerModule(new GuavaModule());
    }
}
