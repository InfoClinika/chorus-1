package com.infoclinika.mssharing.platform.model.helper;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.AnnotationTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;

/**
 * @author Herman Zamula
 */
public interface ManagerTransformersTemplate {

    Function<ExperimentManagementTemplate.AnnotationTemplate, AnnotationTemplate> annotationTransformer(
        boolean persist
    );

}
