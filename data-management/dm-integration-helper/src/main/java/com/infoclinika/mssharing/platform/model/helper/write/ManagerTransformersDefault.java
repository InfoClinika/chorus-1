package com.infoclinika.mssharing.platform.model.helper.write;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.AnnotationTemplate;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.helper.ManagerTransformersTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import com.infoclinika.mssharing.platform.repository.AnnotationRepositoryTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Herman Zamula
 */
@Component
public class ManagerTransformersDefault implements ManagerTransformersTemplate {

    @Inject
    protected EntityFactories entityFactories;
    @Inject
    protected AnnotationRepositoryTemplate annotationRepository;

    public Function<ExperimentManagementTemplate.AnnotationTemplate, AnnotationTemplate> annotationTransformer(final boolean persist) {

        return new Function<ExperimentManagementTemplate.AnnotationTemplate, AnnotationTemplate>() {
            @Override
            public AnnotationTemplate apply(ExperimentManagementTemplate.AnnotationTemplate input) {
                final AnnotationTemplate annotation = entityFactories.annotation.get();
                annotation.setName(input.name);
                annotation.setUnits(input.units);
                annotation.setType(input.isNumeric ? AnnotationTemplate.Type.INTEGER : AnnotationTemplate.Type.STRING);
                annotation.setValue(input.value);
                return persistIfNeeded(annotationRepository, annotation, persist);
            }
        };
    }

    protected <E> E persistIfNeeded(JpaRepository<E, Long> annotationRepository, E toPersistObject, boolean persist) {
        return persist ? annotationRepository.save(toPersistObject) : toPersistObject;
    }
}
