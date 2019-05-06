package com.infoclinika.mssharing.platform.model.impl.helper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.model.TransformersTemplate;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.FileItem;
import com.infoclinika.mssharing.platform.model.helper.UploadHelperTemplate;
import com.infoclinika.mssharing.platform.repository.FileRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.SpeciesRepositoryTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static com.google.common.collect.FluentIterable.from;

/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultUploadHelper implements UploadHelperTemplate {

    @Inject
    protected TransformersTemplate transformers;
    @Inject
    protected SpeciesRepositoryTemplate<?> speciesRepository;
    @Inject
    protected FileRepositoryTemplate<?> fileMetaDataRepository;

    @Override
    public ImmutableSet<DictionaryItem> species() {

        return from(speciesRepository.findAll())
            .transform(transformers.dictionaryItemTransformer())
            .toSet();

    }

    @Override
    public ImmutableSortedSet<FileItem> existingFiles(long actor, long instrument) {

        return from(fileMetaDataRepository.findAvailableByInstrument(actor, instrument))
            .transform(transformers.fileTransformer())
            .toSortedSet(transformers.dictionaryItemComparator());

    }
}
