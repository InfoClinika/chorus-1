package com.infoclinika.mssharing.platform.model.helper.read.details;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.Sharing.Access;
import com.infoclinika.mssharing.platform.model.TransformersTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.SharedPerson;
import com.infoclinika.mssharing.platform.model.read.RequestsDetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.RequestsDetailsReaderTemplate.UserItem;

import java.util.Map;

import static com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.*;

/**
 * @author Herman Zamula
 */
public interface DetailsTransformersTemplate extends TransformersTemplate {

    <A extends Attachment> Function<A, AttachmentItem> attachmentTransformer();

    <U extends UserTemplate> Function<Map.Entry<U, Access>, SharedPerson> sharedPersonAccessTransformer();

    <G extends GroupTemplate> Function<Map.Entry<G, Access>, SharedGroup> groupAccessTransformer();

    <C extends Condition> Function<C, ConditionItem> conditionsTransformer();

    <F extends ExperimentFileTemplate> Function<F, FileItemTemplate> experimentFileTransformer();

    <F extends FactorTemplate> Function<F, MetaFactorTemplate> factorsTransformer();

    <A extends AnnotationTemplate> Function<A, AnnotationItem> annotationsTransformer();

    <U extends UserTemplate> Function<U, SharedPerson> sharedPersonTransformer();

    <L extends LabTemplate> Function<L, LabItemTemplate> labItemTransformer();

    <U extends UserTemplate> Function<U, UserItem> userItemTransformer();
}
