package com.infoclinika.mssharing.platform.model.helper.read.details;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.Sharing.Access;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.DefaultTransformers;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.*;
import com.infoclinika.mssharing.platform.model.read.RequestsDetailsReaderTemplate.UserItem;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.infoclinika.mssharing.platform.entity.AnnotationTemplate.Type.INTEGER;
import static java.util.stream.Collectors.toList;

/**
 * Configured in xml file
 *
 * @author Herman Zamula
 */
public class DetailsTransformers extends DefaultTransformers implements DetailsTransformersTemplate {

    @Override
    public <A extends Attachment> Function<A, DetailsReaderTemplate.AttachmentItem> attachmentTransformer() {
        return input -> new DetailsReaderTemplate.AttachmentItem(
            input.getId(),
            input.getName(),
            input.getSizeInBytes(),
            input.getUploadDate(),
            input.getOwner().getId()
        );
    }

    @Override
    public <U extends UserTemplate> Function<Map.Entry<U, Access>, SharedPerson> sharedPersonAccessTransformer() {
        return input -> {
            final U user = input.getKey();
            return new SharedPerson(
                user.getId(), user.getFullName(), user.getEmail(), input.getValue() == Access.WRITE
            );

        };
    }

    @Override
    public <G extends GroupTemplate> Function<Map.Entry<G, Access>, SharedGroup> groupAccessTransformer() {
        return input -> {
            GroupTemplate group = input.getKey();
            return new SharedGroup(
                group.getId(),
                group.getName(),
                group.getNumberOfMembers(),
                input.getValue() == Access.WRITE
            );
        };
    }


    @Override
    public <C extends Condition> Function<C, ConditionItem> conditionsTransformer() {
        return input -> new ConditionItem(input.getId(), input.getName(), input.getExperiment().getName());
    }

    @Override
    public <F extends ExperimentFileTemplate> Function<F, FileItemTemplate> experimentFileTransformer() {
        return input -> {
            final FileMetaDataTemplate file = input.getFileMetaData();
            final InstrumentTemplate instrument = file.getInstrument();
            final UserTemplate owner = file.getOwner();
            final String specieName = file.getSpecie() == null ? null : file.getSpecie().getName();
            final List<DetailsReaderTemplate.AnnotationItem> annotationItems =
                ((List<AnnotationTemplate>) input.getAnnotationList())
                    .stream()
                    .map(this.annotationsTransformer()::apply)
                    .sorted(Comparator.comparing(o -> o.name))
                    .collect(toList());

            return new FileItemTemplate(
                file.getId(),
                file.getSizeInBytes(),
                file.getUploadDate(),
                file.getLabels(),
                file.getBucket(),
                file.getContentId(),
                owner.getFullName(),
                owner.getEmail(),
                file.isCopy(),
                file.getName(),
                specieName,
                instrument.getName(),
                instrument.getLab().getName(),
                instrument.getId(),
                ImmutableList.of(),
                annotationItems
            );
        };
    }

    public <A extends AnnotationTemplate> Function<A, DetailsReaderTemplate.AnnotationItem> annotationsTransformer() {
        return input -> new AnnotationItem(
            input.getId(),
            input.getNameWithUnits(),
            input.getValue(),
            input.getUnits(),
            input.getType().equals(INTEGER)
        );
    }

    @Override
    public <F extends FactorTemplate> Function<F, MetaFactorTemplate> factorsTransformer() {
        return input -> {
            final Long experimentId = input.getExperiment().getId();
            return new MetaFactorTemplate(
                input.getId(),
                input.getName(),
                input.getUnits(),
                input.getType() == FactorTemplate.Type.INTEGER,
                experimentId
            );
        };
    }

    @Override
    public <U extends UserTemplate> Function<U, SharedPerson> sharedPersonTransformer() {
        return input -> new SharedPerson(input.getId(), input.getFullName(), input.getEmail(), false);
    }

    @Override
    public <L extends LabTemplate> Function<L, DetailsReaderTemplate.LabItemTemplate> labItemTransformer() {
        return lab -> {

            final UserTemplate head = lab.getHead();

            return new LabItemTemplate(
                lab.getId(),
                lab.getName(),
                lab.getInstitutionUrl(),
                head.getFirstName(),
                head.getLastName(),
                head.getEmail(),
                lab.getContactEmail(),
                lab.getLastModification()
            );
        };
    }

    @Override
    public <U extends UserTemplate> Function<U, UserItem> userItemTransformer() {
        return input -> new UserItem(input.getId(), input.getEmail(), input.getFullName());
    }
}
