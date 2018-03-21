package com.infoclinika.mssharing.platform.model.helper.read.details;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.DefaultTransformers;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ConditionItem;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.FileItemTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.MetaFactorTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.SharedPerson;
import com.infoclinika.mssharing.platform.model.read.RequestsDetailsReaderTemplate.UserItem;

import java.util.ArrayList;
import java.util.Map;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.platform.entity.AnnotationTemplate.Type.INTEGER;

/**
 * Configured in xml file
 *
 * @author Herman Zamula
 */
public class DetailsTransformers extends DefaultTransformers implements DetailsTransformersTemplate {

    @Override
    public <A extends Attachment> Function<A, DetailsReaderTemplate.AttachmentItem> attachmentTransformer() {
        return new Function<A, DetailsReaderTemplate.AttachmentItem>() {
            @Override
            public DetailsReaderTemplate.AttachmentItem apply(A input) {
                return new DetailsReaderTemplate.AttachmentItem(
                        input.getId(),
                        input.getName(),
                        input.getSizeInBytes(),
                        input.getUploadDate(),
                        input.getOwner().getId()
                );
            }
        };
    }

    @Override
    public <U extends UserTemplate> Function<Map.Entry<U, Sharing.Access>, SharedPerson> sharedPersonAccessTransformer() {
        return new Function<Map.Entry<U, Sharing.Access>, SharedPerson>() {
            @Override
            public SharedPerson apply(Map.Entry<U, Sharing.Access> input) {
                final U user = input.getKey();
                return new SharedPerson(user.getId(), user.getFullName(), user.getEmail(), input.getValue() == Sharing.Access.WRITE);

            }
        };
    }

    @Override
    public <G extends GroupTemplate> Function<Map.Entry<G, Sharing.Access>, DetailsReaderTemplate.SharedGroup> groupAccessTransformer() {
        return new Function<Map.Entry<G, Sharing.Access>, DetailsReaderTemplate.SharedGroup>() {
            @Override
            public DetailsReaderTemplate.SharedGroup apply(Map.Entry<G, Sharing.Access> input) {
                GroupTemplate group = input.getKey();
                return new DetailsReaderTemplate.SharedGroup(group.getId(), group.getName(), group.getNumberOfMembers(), input.getValue() == Sharing.Access.WRITE);
            }
        };
    }


    @Override
    public <C extends Condition> Function<C, ConditionItem> conditionsTransformer() {
        return new Function<C, ConditionItem>() {
            @Override
            public ConditionItem apply(C input) {
                return new ConditionItem(input.getId(), input.getName(), input.getExperiment().getName());
            }
        };
    }

    @Override
    public <F extends ExperimentFileTemplate> Function<F, FileItemTemplate> experimentFileTransformer() {
        return new Function<F, FileItemTemplate>() {
            @Override
            public FileItemTemplate apply(F input) {

                final FileMetaDataTemplate file = input.getFileMetaData();
                final InstrumentTemplate instrument = file.getInstrument();
                final UserTemplate owner = file.getOwner();
                final String specieName = file.getSpecie() == null ? null : file.getSpecie().getName();

                return new FileItemTemplate(
                        file.getId(),
                        file.getSizeInBytes(),
                        file.getUploadDate(),
                        file.getLabels(),
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
                        ImmutableList.of());
            }
        };
    }

    public <A extends AnnotationTemplate> Function<A, DetailsReaderTemplate.AnnotationItem> annotationsTransformer() {
        return new Function<A, DetailsReaderTemplate.AnnotationItem>() {
            @Override
            public DetailsReaderTemplate.AnnotationItem apply(A input) {
                return new DetailsReaderTemplate.AnnotationItem(input.getId(),
                        input.getNameWithUnits(),
                        input.getValue(),
                        input.getUnits(),
                        input.getType().equals(INTEGER));
            }
        };
    }

    @Override
    public <F extends FactorTemplate> Function<F, MetaFactorTemplate> factorsTransformer() {
        return new Function<F, MetaFactorTemplate>() {
            @Override
            public MetaFactorTemplate apply(F input) {
                final Long experimentId = input.getExperiment().getId();
                return new MetaFactorTemplate(input.getId(), input.getName(), input.getUnits(), input.getType() == FactorTemplate.Type.INTEGER, experimentId);
            }
        };
    }

    @Override
    public <U extends UserTemplate> Function<U, SharedPerson> sharedPersonTransformer() {
        return new Function<U, SharedPerson>() {
            @Override
            public SharedPerson apply(U input) {
                return new SharedPerson(input.getId(), input.getFullName(), input.getEmail(), false);
            }
        };
    }

    @Override
    public <L extends LabTemplate> Function<L, DetailsReaderTemplate.LabItemTemplate> labItemTransformer() {
        return new Function<L, DetailsReaderTemplate.LabItemTemplate>() {
            @Override
            public DetailsReaderTemplate.LabItemTemplate apply(L lab) {

                final UserTemplate head = lab.getHead();

                return new DetailsReaderTemplate.LabItemTemplate(lab.getId(),
                        lab.getName(),
                        lab.getInstitutionUrl(),
                        head.getFirstName(),
                        head.getLastName(),
                        head.getEmail(),
                        lab.getContactEmail(),
                        lab.getLastModification());
            }
        };
    }

    @Override
    public <U extends UserTemplate> Function<U, UserItem> userItemTransformer() {
        return new Function<U, UserItem>() {
            @Override
            public UserItem apply(U input) {
                return new UserItem(input.getId(), input.getEmail(), input.getFullName());
            }
        };
    }
}
