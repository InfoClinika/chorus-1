package com.infoclinika.mssharing.platform.model.helper.write;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.platform.entity.AnnotationTemplate;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentData;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.InstrumentRestriction;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.InvalidFactorException;
import com.infoclinika.mssharing.platform.model.helper.ManagerTransformersTemplate;
import com.infoclinika.mssharing.platform.repository.ExperimentFileRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.ExperimentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.FactorRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.ProjectRepositoryTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Functions.compose;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.*;

/**
 * @author Herman Zamula
 */
@Component
@SuppressWarnings("unchecked")
public class ExperimentManager<EXPERIMENT extends ExperimentTemplate, EXPERIMENT_INFO extends ExperimentInfoTemplate> {

    @Inject
    private ExperimentRepositoryTemplate<EXPERIMENT> experimentRepository;
    @Inject
    private FactorRepositoryTemplate<FactorTemplate> factorRepository;
    @Inject
    private ExperimentFileRepositoryTemplate<ExperimentFileTemplate> experimentFileRepository;
    @Inject
    private ProjectRepositoryTemplate<ProjectTemplate> projectRepository;
    @Inject
    private EntityFactories factories;
    @Inject
    private ManagerTransformersTemplate managerTransformers;


    public EXPERIMENT createWithoutSaving(long creator, EXPERIMENT_INFO info) {
        return doCreateExperiment(creator, info);
    }

    public EXPERIMENT createAndSave(long creator, EXPERIMENT_INFO info) {
        EXPERIMENT experiment = doCreateExperiment(creator, info);
        return saveExperiment(experiment);
    }

    public EXPERIMENT updateExperiment(long experimentId, EXPERIMENT_INFO info) {
        final EXPERIMENT experiment = experimentRepository.findOne(experimentId);
        updateExperimentDetails(experiment, info);
        return saveExperiment(experiment);
    }

    private void updateExperimentDetails(EXPERIMENT experiment, EXPERIMENT_INFO info) {
        final ExperimentData experimentData = new ExperimentData(info.description, info.is2dLc);
        final InstrumentRestriction<? extends InstrumentTemplate> instrumentRestriction = new InstrumentRestriction(
            factories.instrumentModelFromId.apply(info.restriction.instrumentModel),
            info.restriction.instrument.transform(factories.instrumentFromId)
        );
        experiment.setProject(factories.projectFromId.apply(info.project));
        experiment.setExperiment(experimentData);
        experiment.setInstrumentRestriction(instrumentRestriction);
        experiment.setName(info.name);
        experiment.setSpecie(factories.speciesFromId.apply(info.specie));
    }


    public void updateExperimentFiles(EXPERIMENT_INFO info, EXPERIMENT experiment,
                                      Function<ExperimentFileTemplate, ExperimentFileTemplate> setFilePropsFn,
                                      Function<FactorTemplate, FactorTemplate> setFactorPropsFn) {

        validateFactors(info.factors, info.files);

        final ImmutableList<FactorTemplate> factors = persistFactors(transform(
            info.factors,
            compose(setFactorPropsFn, transformFactors(experiment, getLevelsOfFactors(copyOf(info.files))))
        ));

        final ImmutableList<ExperimentFileTemplate> rawFilesData = persistExperimentFiles(constructRawFiles(info.files,
            experiment.getExperiment(), factors, setFilePropsFn
        ));

        experiment.rawFiles.getData().clear();
        experiment.rawFiles.getFactors().clear();
        experiment.rawFiles.getData().addAll(rawFilesData);
        experiment.rawFiles.getFactors().addAll(factors);

        addConditionsToLevelsAndRawFiles(experiment);

    }

    public EXPERIMENT saveExperiment(EXPERIMENT experiment) {
        experiment.setLastModification(new Date());
        return experimentRepository.save(experiment);
    }

    public void deleteExperiment(long experimentId) {
        experimentRepository.delete(experimentId);
    }

    private List<Set<String>> getLevelsOfFactors(Collection<? extends FileItemTemplate> files) {
        final List<Set<String>> factorValuesList = newArrayList();
        for (FileItemTemplate file : files) {
            for (int i = 0; i < file.factorValues.size(); i++) {
                final Set<String> valuesSet;
                if (factorValuesList.size() > i) {
                    valuesSet = factorValuesList.get(i);
                } else {
                    valuesSet = new HashSet<>();
                    factorValuesList.add(i, valuesSet);
                }
                valuesSet.add(file.factorValues.get(i));
            }
        }
        return factorValuesList;
    }

    private EXPERIMENT doCreateExperiment(long creator, EXPERIMENT_INFO info) {
        final EXPERIMENT experimentTemplate = (EXPERIMENT) factories.experiment.get();

        final ExperimentData experimentData = new ExperimentData(info.description, info.is2dLc);
        final InstrumentRestriction restriction =
            new InstrumentRestriction(
                factories.instrumentModelFromId.apply(info.restriction.instrumentModel),
                info.restriction.instrument.transform(factories.instrumentFromId)
            );
        final Species species = factories.speciesFromId.apply(info.specie);

        experimentTemplate.setLab(fromNullable(info.lab).transform(factories.labFromId).orNull());
        experimentTemplate.setProject(factories.projectFromId.apply(info.project));
        experimentTemplate.setCreator(factories.userFromId.apply(creator));
        experimentTemplate.setName(info.name);
        experimentTemplate.setExperiment(experimentData);
        experimentTemplate.setInstrumentRestriction(restriction);
        experimentTemplate.setSpecie(species);
        experimentTemplate.setExperimentType(factories.experimentTypeFromId.apply(info.experimentType));
        setDownloadToken(projectRepository.findOne(info.project).getSharing().getType(), experimentTemplate);

        return experimentTemplate;

    }

    protected ImmutableList<FactorTemplate> persistFactors(Iterable<FactorTemplate> original) {
        return copyOf(factorRepository.save(original));
    }

    private ImmutableList<ExperimentFileTemplate> persistExperimentFiles(
        Iterable<ExperimentFileTemplate> experimentFiles) {
        return copyOf(experimentFileRepository.save(experimentFiles));
    }

    private void setDownloadToken(Sharing.Type type, ExperimentTemplate experiment) {
        switch (type) {
            case PUBLIC:
                experiment.setDownloadToken(String.valueOf(new Random().nextLong()));
                break;
            case SHARED:
            case PRIVATE:
                experiment.setDownloadToken(null);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized sharing type: " + type);

        }
    }

    public Function<MetaFactorTemplate, FactorTemplate> transformFactors(final EXPERIMENT experiment,
                                                                         List<Set<String>> levelsOfFactors) {
        final Iterator<Set<String>> iterator = levelsOfFactors.iterator();
        return input -> {

            final Set<String> levelValues = iterator.next();
            final FactorTemplate factorTemplate = factories.factor.get();
            factorTemplate.setName(input.name);
            factorTemplate.setExperiment(experiment);
            factorTemplate.getLevels().addAll(from(levelValues).transform(level -> {
                final LevelTemplate template = factories.level.get();
                template.setName(level);
                template.setFactor(factorTemplate);
                return template;
            }).toList());
            factorTemplate.setType(input.isNumeric ? FactorTemplate.Type.INTEGER : FactorTemplate.Type.STRING);
            factorTemplate.setUnits(input.units);
            return factorTemplate;
        };
    }

    ImmutableCollection<ExperimentFileTemplate> constructRawFiles(final Collection<? extends FileItemTemplate> files,
                                                                  final ExperimentData experiment,
                                                                  final Iterable<FactorTemplate> factors,
                                                                  Function<ExperimentFileTemplate,
                                                                      ExperimentFileTemplate> setFilePropsFn) {

        return copyOf(Collections2
            .transform(files, compose(setFilePropsFn, (Function<FileItemTemplate, ExperimentFileTemplate>) input -> {

                checkArgument(input.factorValues.size() == size(factors));
                checkArgument(input.factorValues.stream().allMatch(factor -> factor != null && !factor.isEmpty()));
                checkArgument(!experiment.is2dLc() || files.stream().allMatch(Objects::nonNull));

                final List<AnnotationTemplate> savedAnnotations =
                    input.annotationValues.stream()
                        .map(managerTransformers.annotationTransformer(true)::apply)
                        .collect(Collectors.toList());

                final ExperimentFileTemplate fileTemplate = factories.rawFile.get();
                fileTemplate.setFileMetaData(factories.fileFromId.apply(input.id));
                fileTemplate.getFactorValues().addAll(input.factorValues);
                fileTemplate.getAnnotationList().clear();
                fileTemplate.getAnnotationList().addAll(savedAnnotations);
                fileTemplate.setCopy(input.copy);

                return fileTemplate;
            }))
        );
    }

    private void addConditionsToLevelsAndRawFiles(EXPERIMENT ex) {
        new ConditionsFactory(ex, ex.rawFiles.getFactors(), ex.rawFiles.getData()).create();
    }


    private void validateFactors(final Iterable<? extends MetaFactorTemplate> factorDescription,
                                 Iterable<? extends FileItemTemplate> files) {
        from(files).allMatch((Predicate<FileItemTemplate>) input -> {
            checkArgument(Iterables.size(factorDescription) == input.factorValues.size());
            for (MetaFactorTemplate each : factorDescription) {
                if (each.name == null || each.name.trim().length() == 0) {
                    throw new InvalidFactorException("No name was specified for meta factor");
                }
            }
            return true;
        });
    }


}
