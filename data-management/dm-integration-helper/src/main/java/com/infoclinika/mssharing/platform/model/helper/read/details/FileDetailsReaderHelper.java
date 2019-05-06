package com.infoclinika.mssharing.platform.model.helper.read.details;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.AbstractReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.SingleResultBuilder;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.AnnotationItem;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ConditionItem;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.FileItemTemplate;
import com.infoclinika.mssharing.platform.repository.ExperimentFileRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.FileRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;

/**
 * @author Herman Zamula
 */
@Component
@Scope(value = "prototype")
public class FileDetailsReaderHelper<FILE extends FileMetaDataTemplate, FILE_ITEM extends FileItemTemplate>
    extends AbstractReaderHelper<FILE, FILE_ITEM, FileItemTemplate> {

    @Inject
    private FileRepositoryTemplate<FILE> fileMetaDataRepository;

    @Inject
    private ExperimentFileRepositoryTemplate<ExperimentFileTemplate> experimentFileRepositoryTemplate;

    @Inject
    private DetailsTransformers detailsTransformers;

    @Override
    public Function<FILE, FileItemTemplate> getDefaultTransformer() {
        return fileMetaData -> {

            final List<ExperimentFileTemplate> rawFiles =
                experimentFileRepositoryTemplate.findByMetaData(fileMetaData.getId());


            final List<ConditionItem> conditions = new ArrayList<>();
            final List<AnnotationItem> annotations = new ArrayList<>();

            if (!rawFiles.isEmpty()) {
                final ExperimentFileTemplate experimentFileTemplate = rawFiles.get(0);

                //noinspection unchecked
                conditions.addAll(from(experimentFileTemplate.getConditions())
                    .transform(detailsTransformers.conditionsTransformer())
                    .toList());

                //noinspection unchecked
                annotations.addAll(from(experimentFileTemplate.getAnnotationList())
                    .transform(detailsTransformers.annotationsTransformer())
                    .toList());

            }

            final InstrumentTemplate instrument = fileMetaData.getInstrument();
            return new FileItemTemplate(
                fileMetaData.getId(),
                fileMetaData.getSizeInBytes(),
                fileMetaData.getUploadDate(),
                fileMetaData.getLabels(),
                fileMetaData.getBucket(),
                fileMetaData.getContentId(),
                fileMetaData.getOwner().getFullName(),
                fileMetaData.getOwner().getEmail(),
                fileMetaData.isCopy(),
                fileMetaData.getName(),
                fileMetaData.getSpecie() == null ? null : fileMetaData.getSpecie().getName(),
                instrument.getName(),
                instrument.getLab().getName(),
                instrument.getId(),
                conditions,
                annotations
            );

        };
    }

    public SingleResultBuilder<FILE, FILE_ITEM> readFile(long id) {
        return SingleResultBuilder.builder(fileMetaDataRepository.findOne(id), activeTransformer);
    }

}
