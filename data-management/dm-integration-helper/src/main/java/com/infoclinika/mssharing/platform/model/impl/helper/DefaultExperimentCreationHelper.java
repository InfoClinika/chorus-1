package com.infoclinika.mssharing.platform.model.impl.helper;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.TransformersTemplate;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.FileItem;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.common.items.NamedItem;
import com.infoclinika.mssharing.platform.model.helper.ExperimentCreationHelperTemplate;
import com.infoclinika.mssharing.platform.repository.*;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;

/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultExperimentCreationHelper<INSTRUMENT extends InstrumentTemplate, FILE extends FileMetaDataTemplate>
        implements ExperimentCreationHelperTemplate {

    @Inject
    private InstrumentRepositoryTemplate<INSTRUMENT> instrumentRepository;
    @Inject
    private FileRepositoryTemplate<FILE> fileMetaDataRepository;

    @Inject
    private ExperimentTypeRepositoryTemplate<ExperimentType> experimentTypeRepository;

    @Inject
    private SpeciesRepositoryTemplate<Species> speciesRepository;

    @Inject
    private ExperimentRepositoryTemplate experimentRepositoryTemplate;

    @Inject
    private TransformersTemplate transformers;

    @Inject
    private ProjectRepositoryTemplate<?> projectRepository;

    @Inject
    private UserRepositoryTemplate<?> userRepository;

    @Override
    public List<DictionaryItem> availableInstrumentModels(long actor, Long lab) {
        return transformAndSortDictionaryItems(instrumentRepository.availableInstrumentModels(actor, lab == null ? 0 : lab));
    }

    @Override
    public List<DictionaryItem> availableInstrumentModels(long actor, @Nullable Long lab, long technologyType, long vendor) {
        return transformAndSortDictionaryItems(instrumentRepository.availableInstrumentModels(actor, lab == null ? 0 : lab, technologyType, vendor));
    }

    @Override
    public List<DictionaryItem> availableInstrumentModels(long actor, @Nullable Long lab, long technologyType, long vendor, long instrumentType) {
        return transformAndSortDictionaryItems(instrumentRepository.availableInstrumentModels(actor, lab == null ? 0 : lab, technologyType, vendor, instrumentType));
    }

    @Override
    public List<DictionaryItem> availableInstrumentTypes(long actor, @Nullable Long lab, long technologyType, long vendor) {
        return transformAndSortDictionaryItems(instrumentRepository.availableInstrumentTypes(lab == null ? 0 : lab, technologyType, vendor));
    }


    private List<DictionaryItem> transformAndSortDictionaryItems(List<DictionaryRepoItem> models) {
        return from(models)
                .transform(transformers.dictionaryItemTransformer())
                .toSortedList(transformers.dictionaryItemComparator());
    }

    @Override
    public List<InstrumentItem> availableInstrumentsByModel(long actor, long model) {

        return from(instrumentRepository.availableInstrumentsByModel(actor, model))
                .transform(transformers.instrumentItemTransformer())
                .toList();
    }

    @Override
    public List<FileItem> availableFilesByInstrumentModel(long actor, long specie, long model, Long lab) {

        return from(fileMetaDataRepository.availableFilesByInstrumentModel(actor, model, specie, lab == null ? 0 : lab))
                .transform(transformers.fileTransformer())
                .toList();
    }

    @Override
    public List<FileItem> availableFilesByInstrument(long actor, long specie, long instrument) {

        return from(fileMetaDataRepository.availableFilesByInstrumentAndSpecie(actor, specie, instrument))
                .transform(transformers.fileTransformer())
                .toList();
    }

    @Override
    public Set<NamedItem> ownedExperiments(long user) {

        //noinspection unchecked
        return from(experimentRepositoryTemplate.findOwned(user))
                .transform(new Function<ExperimentTemplate, NamedItem>() {
                    @Override
                    public NamedItem apply(ExperimentTemplate input) {
                        return new NamedItem(input.getId(), input.getName());
                    }
                })
                .toSet();

    }

    @Override
    public ImmutableSortedSet<NamedItem> availableProjects(long actor) {

        return from(projectRepository.findAllowedForWriting(actor))
                .transform(new Function<ProjectTemplate, NamedItem>() {
                    @Override
                    public NamedItem apply(ProjectTemplate input) {
                        return new NamedItem(input.getId(), input.getName());
                    }
                })
                .toSortedSet(transformers.namedItemComparator());

    }

    @Override
    public ImmutableSortedSet<NamedItem> availableLabs(long actor) {

        final UserTemplate<?> user = checkNotNull(userRepository.findOne(actor), "User not found. Id=" + actor);

        return from(user.getLabs())
                .transform(new Function<LabTemplate<?>, NamedItem>() {
                    @Override
                    public NamedItem apply(LabTemplate<?> input) {
                        return new NamedItem(input.getId(), input.getName());
                    }
                })
                .toSortedSet(transformers.namedItemComparator());

    }

    @Override
    public ImmutableSortedSet<ExperimentTypeItem> experimentTypes() {

        return from(experimentTypeRepository.findAll())
                .transform(new Function<ExperimentType, ExperimentTypeItem>() {
                    @Override
                    public ExperimentTypeItem apply(ExperimentType type) {
                        return new ExperimentTypeItem(type.getId(), type.getName(), type.allowed2dLC, type.labelsAllowed);
                    }
                })
                .toSortedSet(transformers.dictionaryItemComparator());

    }


    @Override
    public ImmutableSet<DictionaryItem> species() {

        return from(speciesRepository.findAll())
                .transform(transformers.dictionaryItemTransformer())
                .toSet();
    }

    @Override
    public DictionaryItem defaultSpecie() {

        Species unspecified = speciesRepository.getUnspecified();
        return transformers.dictionaryItemTransformer().apply(unspecified);

    }

    @Override
    public DictionaryItem specie(long id) {
        return transformers.dictionaryItemTransformer().apply(speciesRepository.findOne(id));
    }

}
