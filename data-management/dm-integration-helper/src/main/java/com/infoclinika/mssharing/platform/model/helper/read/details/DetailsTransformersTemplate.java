package com.infoclinika.mssharing.platform.model.helper.read.details;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.model.TransformersTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.RequestsDetailsReaderTemplate;

import java.util.Map;

/**
 * @author Herman Zamula
 */
public interface DetailsTransformersTemplate extends TransformersTemplate {

    <A extends Attachment> Function<A, DetailsReaderTemplate.AttachmentItem> attachmentTransformer();

    <U extends UserTemplate> Function<Map.Entry<U, Sharing.Access>, DetailsReaderTemplate.SharedPerson> sharedPersonAccessTransformer();

    <G extends GroupTemplate> Function<Map.Entry<G, Sharing.Access>, DetailsReaderTemplate.SharedGroup> groupAccessTransformer();

    <C extends Condition> Function<C, DetailsReaderTemplate.ConditionItem> conditionsTransformer();

    <F extends ExperimentFileTemplate> Function<F, DetailsReaderTemplate.FileItemTemplate> experimentFileTransformer();

    <F extends FactorTemplate> Function<F, DetailsReaderTemplate.MetaFactorTemplate> factorsTransformer();

    <A extends AnnotationTemplate> Function<A, DetailsReaderTemplate.AnnotationItem> annotationsTransformer();

    <U extends UserTemplate> Function<U, DetailsReaderTemplate.SharedPerson> sharedPersonTransformer();

    <L extends LabTemplate> Function<L, DetailsReaderTemplate.LabItemTemplate> labItemTransformer();

    <U extends UserTemplate> Function<U, RequestsDetailsReaderTemplate.UserItem> userItemTransformer();
}
