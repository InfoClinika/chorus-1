package com.infoclinika.mssharing.platform.model.impl.write;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.entity.ExperimentType;
import com.infoclinika.mssharing.platform.entity.FactorTemplate;
import com.infoclinika.mssharing.platform.entity.RawFiles;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.ActionsNotAllowedException;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.helper.write.ExperimentManager;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import com.infoclinika.mssharing.platform.repository.ExperimentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.ExperimentTypeRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.ProjectRepositoryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkAccess;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;

/**
 * @author : Alexander Serebriyan
 */
@Component
@Transactional
public class DefaultExperimentManagement<EXPERIMENT extends ExperimentTemplate,
    EXPERIMENT_INFO extends ExperimentManagementTemplate.ExperimentInfoTemplate>
    implements ExperimentManagementTemplate<EXPERIMENT_INFO> {

    public static final Function<FileItemTemplate, Long> FILE_ITEM_TO_ID = new Function<FileItemTemplate, Long>() {
        @Override
        public Long apply(FileItemTemplate input) {
            return input.id;
        }
    };
    @Inject
    protected ExperimentManager<EXPERIMENT, EXPERIMENT_INFO> experimentManager;
    @Inject
    protected RuleValidator ruleValidator;
    @Inject
    protected ExperimentRepositoryTemplate<EXPERIMENT> experimentRepository;
    @Inject
    protected ProjectRepositoryTemplate<?> projectRepository;
    @Inject
    protected ExperimentTypeRepositoryTemplate<? extends ExperimentType> experimentTypeRepository;

    @Override
    public long createExperiment(long actor, EXPERIMENT_INFO experimentInfo) {
        beforeCreateExperiment(actor, experimentInfo);

        final EXPERIMENT experiment = onCreateExperiment(actor, experimentInfo);

        Function<ExperimentFileTemplate, ExperimentFileTemplate> setFilePropsFn = createSetRawFilesPropsFn();
        Function<FactorTemplate, FactorTemplate> setFactorPropsFn = createSetFactorsPropsFn();
        EXPERIMENT experimentAfterFilesUpdate =
            updateExperimentFilesOnCreateExperiment(experimentInfo, experiment, setFilePropsFn, setFactorPropsFn);

        EXPERIMENT finalExperiment = afterCreateExperiment(experimentAfterFilesUpdate, experimentInfo);

        return finalExperiment.getId();
    }

    protected EXPERIMENT onCreateExperiment(long actor, EXPERIMENT_INFO experimentInfo) {
        return experimentManager.createAndSave(actor, experimentInfo);
    }

    protected EXPERIMENT updateExperimentFilesOnCreateExperiment(
        EXPERIMENT_INFO experimentInfo,
        EXPERIMENT experiment,
        Function<ExperimentFileTemplate, ExperimentFileTemplate> setFilePropsFn,
        Function<FactorTemplate, FactorTemplate> setFactorPropsFn) {

        experimentManager.updateExperimentFiles(experimentInfo, experiment, setFilePropsFn, setFactorPropsFn);
        return experiment;
    }

    private Function<FactorTemplate, FactorTemplate> createSetFactorsPropsFn() {
        return input -> input;
    }

    private Function<ExperimentFileTemplate, ExperimentFileTemplate> createSetRawFilesPropsFn() {
        return input -> input;
    }


    protected EXPERIMENT afterCreateExperiment(EXPERIMENT experiment, EXPERIMENT_INFO experimentInfo) {
        return experimentManager.saveExperiment(experiment);
    }

    @SuppressWarnings("unchecked")
    protected void beforeCreateExperiment(long actor, EXPERIMENT_INFO experimentInfo) {

        if (!ruleValidator.canUserPerformActions(actor)) {
            throw new ActionsNotAllowedException(actor);
        }

        checkArgument(
            experimentInfo.files != null && !experimentInfo.files.isEmpty(), "Can't create experiment without files");
        checkNotNull(
            experimentTypeRepository.findOne(experimentInfo.experimentType),
            "Can't create experiment without experiment type"
        );
        final ImmutableSet<Long> fileIds = from(experimentInfo.files).transform(FILE_ITEM_TO_ID).toSet();
        checkArgument(
            ruleValidator.canUserCreateExperimentWithTitle(actor, experimentInfo.name),
            "User already has experiment with this name: \"" + experimentInfo.name + "\""
        );
        checkArgument(
            ruleValidator.canSaveExperimentWithFiles(transform(experimentInfo.files, FILE_ITEM_TO_ID)),
            "Can't create experiment with files specified"
        );
        checkState(
            ruleValidator.canSaveExperimentWithModel(experimentInfo.restriction.instrumentModel, fileIds),
            "Can't create experiment with selected files and instrument model"
        );
        checkState(
            ruleValidator.canSaveExperimentWithSpecies(experimentInfo.specie, fileIds),
            "Can't create experiment with selected files and species"
        );
        checkAccess(
            ruleValidator.canUserCreateExperimentsInProject(actor, experimentInfo.project),
            "User has no permissions to create experiment in this project"
        );
    }

    @Override
    public void deleteExperiment(long actor, long experiment) {
        beforeDeleteExperiment(actor, experiment);

        experimentManager.deleteExperiment(experiment);
    }

    protected void beforeDeleteExperiment(long actor, long experiment) {
        if (!ruleValidator.canUserPerformActions(actor)) {
            throw new ActionsNotAllowedException(actor);
        }
        checkAccess(ruleValidator.canRemoveExperiment(actor, experiment), "Couldn't remove experiment: " + experiment);
    }

    @Override
    public void updateExperiment(long actor, long experimentId, EXPERIMENT_INFO experimentInfo) {

        beforeUpdateExperiment(actor, experimentId, experimentInfo);

        //noinspection unchecked
        final boolean filesChanged =
            wereExperimentFilesChanged(experimentId, experimentInfo.project, experimentInfo.restriction,
                experimentInfo.factors, experimentInfo.files
            );

        EXPERIMENT experiment = onUpdateExperiment(experimentId, experimentInfo);

        Function<FactorTemplate, FactorTemplate> setFactorsPropsFn = createSetFactorsPropsFn();
        Function<ExperimentFileTemplate, ExperimentFileTemplate> setRawFilesPropsFn = createSetRawFilesPropsFn();
        if (filesChanged) {
            updateExperimentFilesOnUpdateExperiment(experimentInfo, experiment, setFactorsPropsFn, setRawFilesPropsFn);
        }

        afterUpdateExperiment(actor, experimentInfo, experiment);
    }

    protected void afterUpdateExperiment(long actor, EXPERIMENT_INFO experimentInfo, EXPERIMENT experiment) {

    }

    protected void updateExperimentFilesOnUpdateExperiment(
        EXPERIMENT_INFO experimentInfo,
        EXPERIMENT experiment,
        Function<FactorTemplate, FactorTemplate> setFactorsPropsFn,
        Function<ExperimentFileTemplate, ExperimentFileTemplate> setRawFilesPropsFn) {
        experimentManager.updateExperimentFiles(experimentInfo, experiment, setRawFilesPropsFn, setFactorsPropsFn);
    }

    protected EXPERIMENT onUpdateExperiment(long experimentId, EXPERIMENT_INFO experimentInfo) {
        return experimentManager.updateExperiment(experimentId, experimentInfo);
    }

    @SuppressWarnings("unchecked")
    protected void beforeUpdateExperiment(long actor, long experimentId, EXPERIMENT_INFO experimentInfo) {

        if (!ruleValidator.canUserPerformActions(actor)) {
            throw new ActionsNotAllowedException(actor);
        }

        final ImmutableSet<Long> fileIds = from(experimentInfo.files).transform(FILE_ITEM_TO_ID).toSet();

        checkPresence(projectRepository.findOne(experimentInfo.project), "Project not found");
        checkNotNull(
            experimentTypeRepository.findOne(experimentInfo.experimentType),
            "Can't update experiment with experiment type not specified %s", experimentInfo.experimentType
        );
        checkState(
            ruleValidator.canSaveExperimentWithModel(experimentInfo.restriction.instrumentModel, fileIds),
            "Can't update experiment with selected files and instrument model"
        );
        checkState(
            ruleValidator.canSaveExperimentWithSpecies(experimentInfo.specie, fileIds),
            "Can't update experiment with selected files and species"
        );

        ImmutableCollection<FileItemTemplate> immutableFiles = copyOf(experimentInfo.files);
        checkArgument(!experimentInfo.files.isEmpty(), "Can't save experiment without files");
        if (!ruleValidator.userHasEditPermissionsOnExperiment(actor, experimentId)
            || !ruleValidator.userHasReadPermissionsOnFiles(actor, getFileMetaDataIds(immutableFiles))) {
            throw new AccessDenied("User isn't permitted to edit experiment");
        }
        checkArgument(
            ruleValidator.canUserUpdateExperimentWithTitle(actor, experimentId, experimentInfo.name),
            "User already has experiment with this name: \"" + experimentInfo.name + "\""
        );
        checkArgument(
            ruleValidator.canSaveExperimentWithFiles(transform(experimentInfo.files, FILE_ITEM_TO_ID)),
            "Can't update experiment with files specified"
        );
    }

    private Iterable<Long> getFileMetaDataIds(ImmutableCollection<FileItemTemplate> files) {
        return Iterables.transform(files, new Function<FileItemTemplate, Long>() {
            @Override
            public Long apply(FileItemTemplate input) {
                return input.id;
            }
        });
    }

    protected boolean wereExperimentFilesChanged(long experimentId, long newProject, Restriction restriction,
                                                 List<MetaFactorTemplate> newFactors, List<FileItemTemplate> newFiles) {
        final EXPERIMENT experiment = experimentRepository.findOne(experimentId);

        final Long oldInstrumentId = experiment.getInstrumentRestriction().getInstrument() == null ? null :
            experiment.getInstrumentRestriction().getInstrument().getId();
        Long newInstrumentId = (restriction.instrument.isPresent()) ? restriction.instrument.get() : null;
        //check instrument and model match
        if (!Objects.equals(oldInstrumentId, newInstrumentId)) {
            return true;
        }
        if (!experiment.getInstrumentRestriction().getInstrumentModel().getId().equals(restriction.instrumentModel)) {
            return true;
        }

        //noinspection unchecked
        final RawFiles<FactorTemplate<?, ?>, ExperimentFileTemplate<?, ?, ?>> rawFilesObj = experiment.getRawFiles();
        final List<ExperimentFileTemplate<?, ?, ?>> oldRawFileList = rawFilesObj.getData();
        final List<FactorTemplate<?, ?>> oldFactors = rawFilesObj.getFilteredFactors();
        if (oldRawFileList.size() != newFiles.size()) {
            return true;
        }
        if (newFactors.size() != oldFactors.size()) {
            return true;
        }
        //check factors names
        final Map<String, MetaFactorTemplate> newFactorsMap =
            Maps.uniqueIndex(newFactors, f -> f.name);

        for (FactorTemplate f : oldFactors) {
            if (!newFactorsMap.containsKey(f.getName())) {
                return true;
            }
        }
        //checking all raw files match new files
        final Map<Long, FileItemTemplate> newFilesMap =
            Maps.uniqueIndex(newFiles, f -> f.id);

        for (ExperimentFileTemplate oldRawFile : oldRawFileList) {
            final long fileMetaDataId = oldRawFile.getFileMetaData().getId();
            if (!newFilesMap.containsKey(fileMetaDataId)) {
                return true;
            }
            final FileItemTemplate newFileItem = newFilesMap.get(fileMetaDataId);
            final HashSet<String> setOfFactorValues = newHashSet(newFileItem.factorValues);
            if (Sets.intersection(setOfFactorValues, newHashSet(oldRawFile.getFactorValues())).size() !=
                setOfFactorValues.size()) {
                return true;
            }
        }
        return false;
    }
}
