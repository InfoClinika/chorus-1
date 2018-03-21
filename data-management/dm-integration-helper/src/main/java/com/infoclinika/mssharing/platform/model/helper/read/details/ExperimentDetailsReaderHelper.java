package com.infoclinika.mssharing.platform.model.helper.read.details;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.InstrumentRestriction;
import com.infoclinika.mssharing.platform.model.DefaultTransformers;
import com.infoclinika.mssharing.platform.model.helper.read.AbstractReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.SingleResultBuilder;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.AttachmentItem;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ExperimentItemTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.FileItemTemplate;
import com.infoclinika.mssharing.platform.repository.ExperimentRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.platform.entity.EntityUtil.ENTITY_TO_ID;

/**
 * @author Herman Zamula
 */
@Component
@Scope(value = "prototype")
public class ExperimentDetailsReaderHelper<EXPERIMENT extends ExperimentTemplate,
        EXPERIMENT_ITEM extends ExperimentItemTemplate>
        extends AbstractReaderHelper<EXPERIMENT, EXPERIMENT_ITEM, ExperimentItemTemplate> {


    @Inject
    private ExperimentRepositoryTemplate<EXPERIMENT> experimentRepository;
    @Inject
    private DetailsTransformersTemplate detailsTransformers;

    @Override
    public Function<EXPERIMENT, ExperimentItemTemplate> getDefaultTransformer() {
        return new Function<EXPERIMENT, ExperimentItemTemplate>() {
            @Override
            @SuppressWarnings("unchecked")
            public ExperimentItemTemplate apply(final ExperimentTemplate ex) {

                final RawFiles rawFiles = ex.getRawFiles();
                final InstrumentRestriction restriction = ex.getInstrumentRestriction();

                final InstrumentModel instrumentModel = restriction.getInstrumentModel();
                final InstrumentTemplate instrument = restriction.getInstrument();
                final InstrumentStudyType technologyType = instrumentModel.getStudyType();
                final Vendor vendor = instrumentModel.getVendor();

                final ImmutableList<DetailsReaderTemplate.MetaFactorTemplate> factors = from(rawFiles.getFactors()).transform(detailsTransformers.factorsTransformer()).toList();
                final ImmutableList<FileItemTemplate> files = from(rawFiles.getData()).transform(detailsTransformers.experimentFileTransformer()).toList();
                final ImmutableList<AttachmentItem> attachments = from(ex.attachments).transform(detailsTransformers.attachmentTransformer()).toList();

                final Optional<LabTemplate> lab = fromNullable(ex.getLab());
                final String labName = lab.isPresent() ? lab.get().getName() : null;
                final Long labHead = lab.isPresent() ? lab.get().getHead().getId() : null;

                final InstrumentType instrumentType = instrumentModel.getType();
                return new ExperimentItemTemplate(
                        ex.getId(),
                        ex.getName(),
                        ex.getProject().getId(),
                        ex.getExperiment().getDescription(),
                        ex.getLastModification(),
                        labName,
                        ex.getNumberOfFiles(),
                        factors,
                        files,
                        ex.getSpecie().getId(),
                        ex.getCreator().getEmail(),
                        technologyType.getId(),
                        ex.getExperimentType().getId(),
                        fromNullable(instrument).transform(ENTITY_TO_ID),
                        collectFactorValues(rawFiles),
                        attachments,
                        instrument == null ? null : instrument.getName(),
                        vendor.getName(),
                        vendor.getId(),
                        instrumentModel.getId(),
                        instrumentType.getId(),
                        lab.transform(ENTITY_TO_ID).orNull(),
                        DefaultTransformers.fromSharingType(ex.getProject().getSharing().getType()),
                        labHead
                );
            }
        };
    }

    public SingleResultBuilder<EXPERIMENT, EXPERIMENT_ITEM> readExperiment(long id) {
        return SingleResultBuilder.builder(experimentRepository.findOne(id), activeTransformer);
    }

    private String[][] collectFactorValues(RawFiles<?, ?> rawFiles) {
        final String[][] res = new String[rawFiles.getData().size()][];
        final int numberOfFactors = Iterables.size(rawFiles.getFilteredFactors());
        int attachmentIndex = 0;
        for (ExperimentFileTemplate rawFile : rawFiles.getData()) {
            res[attachmentIndex] = new String[numberOfFactors];
            //noinspection unchecked
            final List<String> factorValues = rawFile.getFactorValues();
            final String[] a = new String[factorValues.size()];
            res[attachmentIndex] = factorValues.toArray(a);
            attachmentIndex++;
        }
        return res;
    }

}
