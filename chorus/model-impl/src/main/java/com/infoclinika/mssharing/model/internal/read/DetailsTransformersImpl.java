package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.model.helper.ExperimentPreparedSampleItem;
import com.infoclinika.mssharing.model.internal.entity.ExperimentPreparedSample;
import com.infoclinika.mssharing.model.internal.entity.RawFile;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.details.DetailsTransformers;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import org.springframework.stereotype.Component;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.model.internal.read.Transformers.transformStorageStatus;

/**
 * @author Herman Zamula
 */
@Component
public class DetailsTransformersImpl extends DetailsTransformers {

    @Override
    public <F extends ExperimentFileTemplate> Function<F, DetailsReaderTemplate.FileItemTemplate>
        experimentFileTransformer() {

        return new Function<F, DetailsReaderTemplate.FileItemTemplate>() {
            @Override
            public DetailsReaderTemplate.FileItemTemplate apply(F input) {

                final DetailsReaderTemplate.FileItemTemplate byDefault =
                    DetailsTransformersImpl.super.experimentFileTransformer().apply(input);

                final ActiveFileMetaData fileMetaData = (ActiveFileMetaData) input.getFileMetaData();
                final RawFile rawFile = (RawFile) input;
                final ExperimentPreparedSample preparedSample = rawFile.getPreparedSample();
                final ExperimentPreparedSampleItem preparedSampleItem = new ExperimentPreparedSampleItem(
                    preparedSample.getName(),
                    from(preparedSample.getSamples()).transform(Transformers.AS_SAMPLE_ITEM).toSet()
                );
                return new FileItem(byDefault,
                    fileMetaData.getArchiveId(),
                    transformStorageStatus(
                        fileMetaData.getStorageData().getStorageStatus(),
                        fileMetaData.getStorageData().isArchivedDownloadOnly()
                    ),
                    rawFile.getPairedEnd(),
                    rawFile.getFractionNumber(),
                    preparedSampleItem
                );
            }
        };
    }
}
