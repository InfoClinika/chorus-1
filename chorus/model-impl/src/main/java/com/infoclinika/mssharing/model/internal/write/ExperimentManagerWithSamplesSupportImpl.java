package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.infoclinika.mssharing.model.helper.ExperimentSampleItem;
import com.infoclinika.mssharing.model.internal.entity.*;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.PrepToExperimentSample;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.repository.ExperimentPreparedSampleRepository;
import com.infoclinika.mssharing.model.internal.repository.ExperimentSampleRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.PrepToExperimentSampleRepository;
import com.infoclinika.mssharing.model.write.ExperimentInfo;
import com.infoclinika.mssharing.model.write.FileItem;
import com.infoclinika.mssharing.platform.entity.FactorTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import com.infoclinika.mssharing.platform.repository.ExperimentFileRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.FactorRepositoryTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author andrii.loboda
 */
@Service
public class ExperimentManagerWithSamplesSupportImpl implements ExperimentManagerWithSamplesSupport<ExperimentInfo, ActiveExperiment> {
    @Inject
    private FactorRepositoryTemplate<Factor> factorRepository;
    @Inject
    private ExperimentFileRepositoryTemplate<RawFile> experimentFileRepository;
    @Inject
    private ExperimentSampleRepository experimentSampleRepository;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    private ExperimentPreparedSampleRepository experimentPreparedSampleRepository;
    @Inject
    private PrepToExperimentSampleRepository prepToExperimentSampleRepository;

    @Override
    public void updateExperimentFilesWithFactorsAndSamples(ExperimentInfo info, ActiveExperiment experiment) {

        //preparing samples to be persisted
        final HashMultimap<String, String> preparedSampleNameToPrepToExSampleKey = HashMultimap.create(); // prepared sample have multiple prepToExSample and each of them have experimentSample
        final Map<Long, String> fileIdToPreparedSamples = newHashMap();
        final Map<String, ExperimentSampleItem> sampleNameToSample = newHashMap();
        final ArrayListMultimap<String, String> sampleNameWithTypeToPreparedSampleName = ArrayListMultimap.<String, String>create();
        for (FileItem file : info.files) {
            fileIdToPreparedSamples.put(file.id, file.preparedSample.name);
            for (ExperimentSampleItem sample : file.preparedSample.samples) {
                final String prepToExSampleKey = constructPrepToExSampleKey(sample.name, Transformers.AS_SAMPLE_TYPE.apply(sample.type));
                sampleNameToSample.put(prepToExSampleKey, sample);
                preparedSampleNameToPrepToExSampleKey.put(file.preparedSample.name, prepToExSampleKey);
                sampleNameWithTypeToPreparedSampleName.put(prepToExSampleKey, file.preparedSample.name);
                //could in one prep sample be several samples with the same name? = yes
                //could in one prep sample be several samples with the same name and the same type ? = no
            }
        }

        final List<ExperimentSampleItem> allSamplesSortedByName = newArrayList(sampleNameToSample.values());
        Collections.sort(allSamplesSortedByName, new Ordering<ExperimentSampleItem>() {
            @Override
            public int compare(ExperimentSampleItem sampleItem, ExperimentSampleItem sampleItem2) {
                return sampleItem.name.compareTo(sampleItem2.name);
            }
        });

        final ImmutableList<Factor> factors = persistFactors(transform(info.factors, transformFactors(experiment, getLevelsOfFactors(copyOf(allSamplesSortedByName)))));

        //saving bio-samples
        final ImmutableList<ExperimentSample> savedSamples = persistExperimentSamples(constructSamples(allSamplesSortedByName));

        //saving prepared-sample
        final ImmutableList<ExperimentPreparedSample> savedPreparedSamples = persistExperimentPreparedSamples(constructPreparedSamples(preparedSampleNameToPrepToExSampleKey.asMap()));
        final Map<String, Collection<ExperimentPreparedSample>> sampleNameWithTypeToPreparedSamples = constructSampleNameToSavedPreparedSampleMap(sampleNameWithTypeToPreparedSampleName.asMap(), savedPreparedSamples);
        //saving prep-sample-to-bio-sample
        //one sample can be used in several prepared samples in the same type and different types either
        final ImmutableList<PrepToExperimentSample> savedPrepToExperimentSamples = persistPrepToExperimentSamples(constructPrepToExSamples(allSamplesSortedByName, savedSamples, sampleNameWithTypeToPreparedSamples));


        final Map<Long, ExperimentPreparedSample> fileIdToSavedPreparedSample = composeFileIdToPersistedPreparedSamples(fileIdToPreparedSamples, savedPreparedSamples);


        //save rawfiles, factors, levels, conditions
        final ImmutableList<RawFile> rawFilesData = persistExperimentFiles(constructRawFiles(info.files, fileIdToSavedPreparedSample));

        experiment.rawFiles.getData().clear();
        experiment.rawFiles.getFactors().clear();
        experiment.rawFiles.getData().addAll(rawFilesData);
        experiment.rawFiles.getFactors().addAll(factors);

        addConditionsToLevelsAndRawFiles(experiment, savedSamples);
    }

    private Map<String, Collection<ExperimentPreparedSample>> constructSampleNameToSavedPreparedSampleMap(Map<String, Collection<String>> sampleNameWithTypeToPreparedSampleName, ImmutableList<ExperimentPreparedSample> savedPreparedSamples) {
        final ImmutableMap<String, ExperimentPreparedSample> prepNameToSavedPreparedSample = Maps.uniqueIndex(savedPreparedSamples, new Function<ExperimentPreparedSample, String>() {
            @Override
            public String apply(ExperimentPreparedSample preparedSample) {
                return preparedSample.getName();
            }
        });
        final ArrayListMultimap<String, ExperimentPreparedSample> sampleNameWithTypeToPreparedSamples = ArrayListMultimap.<String, ExperimentPreparedSample>create();
        for (Map.Entry<String, Collection<String>> sampleNameWithTypeToPreparedSampleNameEntry : sampleNameWithTypeToPreparedSampleName.entrySet()) {
            final String sampleNameWithType = sampleNameWithTypeToPreparedSampleNameEntry.getKey();
            final Collection<String> preparedSamplesList = sampleNameWithTypeToPreparedSampleNameEntry.getValue();
            for (String preparedSampleName : preparedSamplesList) {
                final ExperimentPreparedSample preparedSample = prepNameToSavedPreparedSample.get(preparedSampleName);
                sampleNameWithTypeToPreparedSamples.put(sampleNameWithType, preparedSample);
            }
        }
        return sampleNameWithTypeToPreparedSamples.asMap();
    }

    private Iterable<PrepToExperimentSample> constructPrepToExSamples(List<ExperimentSampleItem> allSamplesSortedByName, ImmutableList<ExperimentSample> savedSamples,
                                                                      Map<String, Collection<ExperimentPreparedSample>> sampleNameWithTypeToPreparedSamples) {
        final Map<String, ExperimentSample> sampleNameToSavedSample = newHashMap();
        final ArrayListMultimap<String, ExperimentSampleItem> sampleNameToItem = ArrayListMultimap.<String, ExperimentSampleItem>create();
        for (ExperimentSample sample : savedSamples) {
            sampleNameToSavedSample.put(sample.getName(), sample);
        }
        for (ExperimentSampleItem sampleItem : allSamplesSortedByName) {
            sampleNameToItem.put(sampleItem.name, sampleItem);
        }
        final List<PrepToExperimentSample> prepToExperimentSamples = newArrayList();
        // for each sample item, find saved sample
        for (Map.Entry<String, Collection<ExperimentSampleItem>> sampleNameToItemEntry : sampleNameToItem.asMap().entrySet()) {

            final ExperimentSample savedSample = sampleNameToSavedSample.get(sampleNameToItemEntry.getKey());
            final Collection<ExperimentSampleItem> diffTypesOfOneSample = sampleNameToItemEntry.getValue();
            for (ExperimentSampleItem sampleItem : diffTypesOfOneSample) {

                // find prepared sample which have this sample with sample type
                final ExperimentSampleType experimentSampleType = Transformers.AS_SAMPLE_TYPE.apply(sampleItem.type);
                final Set<ExperimentPreparedSample> prepSamplesWhichContainsSampleWithType = findPreparedSampleWithSample(sampleItem.name, experimentSampleType, sampleNameWithTypeToPreparedSamples);
                // add prep to experiment samples for specific sample for specific type(LIGHT, HEAVY)
                for (ExperimentPreparedSample preparedSample : prepSamplesWhichContainsSampleWithType) {
                    prepToExperimentSamples.add(new PrepToExperimentSample(preparedSample, savedSample, experimentSampleType));
                }
            }

        }
        return prepToExperimentSamples;
    }

    private Set<ExperimentPreparedSample> findPreparedSampleWithSample(String name, ExperimentSampleType experimentSampleType, Map<String, Collection<ExperimentPreparedSample>> sampleNameWithTypeToPreparedSamples) {
        return newHashSet(sampleNameWithTypeToPreparedSamples.get(constructPrepToExSampleKey(name, experimentSampleType)));
    }

    private Iterable<ExperimentPreparedSample> constructPreparedSamples(Map<String, Collection<String>> preparedSampleNameToSamples) {
        Set<ExperimentPreparedSample> preparedSamplesToPersist = newHashSet();

        for (Map.Entry<String, Collection<String>> prepSampleToSamples : preparedSampleNameToSamples.entrySet()) { // for each prepared sample name create an entity
            final String prepSampleName = prepSampleToSamples.getKey();
            preparedSamplesToPersist.add(new ExperimentPreparedSample(prepSampleName, newHashSet()));
        }
        return preparedSamplesToPersist;
    }

    private static String constructPrepToExSampleKey(String bioSampleName, ExperimentSampleType sampleType) {
        return bioSampleName + sampleType.toString();
    }


    private Map<Long, ExperimentPreparedSample> composeFileIdToPersistedPreparedSamples(Map<Long, String> fileIdToPreparedSamples, ImmutableList<ExperimentPreparedSample> savedPreparedSamples) {
        Map<Long, ExperimentPreparedSample> fileIdToPreparedSample = newHashMap();
        final ImmutableMap<String, ExperimentPreparedSample> prepSampleNameToPrepSample = Maps.uniqueIndex(savedPreparedSamples, new Function<ExperimentPreparedSample, String>() {
            @Override
            public String apply(ExperimentPreparedSample preparedSample) {
                return preparedSample.getName();
            }
        });
        for (Map.Entry<Long, String> fileIdToPrepSampleEntry : fileIdToPreparedSamples.entrySet()) {
            fileIdToPreparedSample.put(fileIdToPrepSampleEntry.getKey(), prepSampleNameToPrepSample.get(fileIdToPrepSampleEntry.getValue()));
        }
        return fileIdToPreparedSample;
    }


    private ImmutableList<Factor> persistFactors(Iterable<Factor> original) {
        return copyOf(factorRepository.save(original));
    }

    private ImmutableList<RawFile> persistExperimentFiles(Iterable<RawFile> experimentFiles) {
        return copyOf(experimentFileRepository.save(experimentFiles));
    }

    private ImmutableList<ExperimentSample> persistExperimentSamples(Iterable<ExperimentSample> samples) {
        return copyOf(experimentSampleRepository.save(samples));
    }

    private ImmutableList<ExperimentPreparedSample> persistExperimentPreparedSamples(Iterable<ExperimentPreparedSample> preparedSamples) {
        return copyOf(experimentPreparedSampleRepository.save(preparedSamples));
    }

    private ImmutableList<PrepToExperimentSample> persistPrepToExperimentSamples(Iterable<PrepToExperimentSample> preparedSamples) {
        return copyOf(prepToExperimentSampleRepository.save(preparedSamples));
    }

    private ImmutableCollection<RawFile> constructRawFiles(final Collection<FileItem> files, Map<Long, ExperimentPreparedSample> fileIdToPreparedSample) {

        return copyOf(Collections2.transform(files, new Function<FileItem, RawFile>() {
            @Override
            public RawFile apply(FileItem input) {

                final RawFile fileTemplate = new RawFile();
                fileTemplate.setFileMetaData(fileMetaDataRepository.findOne(input.id));
                fileTemplate.setCopy(input.copy);
                fileTemplate.setPreparedSample(fileIdToPreparedSample.get(input.id));
                fileTemplate.setFractionNumber(input.fractionNumber);
                return fileTemplate;
            }
        }));
    }

    private static ImmutableCollection<ExperimentSample> constructSamples(final Collection<ExperimentSampleItem> samples) {
        final Map<String, ExperimentSampleItem> samplesWithoutDuplicates = newHashMap();
        for (ExperimentSampleItem sample : samples) {
            samplesWithoutDuplicates.put(sample.name, sample);
        }
        return copyOf(Collections2.transform(samplesWithoutDuplicates.values(), new Function<ExperimentSampleItem, ExperimentSample>() {
            @Override
            public ExperimentSample apply(ExperimentSampleItem sampleItem) {
                final ExperimentSample sample = new ExperimentSample();
                sample.getFactorValues().addAll(sampleItem.factorValues);
                sample.setName(sampleItem.name);
                return sample;
            }
        }));
    }

    private static List<Set<String>> getLevelsOfFactors(Iterable<ExperimentSampleItem> sampleItems) {
        final List<Set<String>> factorValuesList = newArrayList();
        for (ExperimentSampleItem sampleItem : sampleItems) {
            for (int i = 0; i < sampleItem.factorValues.size(); i++) {
                final Set<String> valuesSet;
                if (factorValuesList.size() > i) {
                    valuesSet = factorValuesList.get(i);
                } else {
                    valuesSet = new HashSet<>();
                    factorValuesList.add(i, valuesSet);
                }
                valuesSet.add(sampleItem.factorValues.get(i));
            }
        }
        return factorValuesList;
    }

    private static Function<ExperimentManagementTemplate.MetaFactorTemplate, Factor> transformFactors(final ActiveExperiment experiment, List<Set<String>> levelsOfFactors) {
        final Iterator<Set<String>> iterator = levelsOfFactors.iterator();
        return new Function<ExperimentManagementTemplate.MetaFactorTemplate, Factor>() {
            @Override
            public Factor apply(ExperimentManagementTemplate.MetaFactorTemplate input) {

                final Set<String> levelValues = iterator.next();
                final Factor factor = new Factor();
                factor.setName(input.name);
                factor.setExperiment(experiment);
                factor.getLevels().addAll(from(levelValues).transform(new Function<String, Level>() {
                    @Override
                    public Level apply(String input) {
                        final Level level = new Level();
                        level.setName(input);
                        level.setFactor(factor);
                        return level;
                    }
                }).toList());
                factor.setType(input.isNumeric ? FactorTemplate.Type.INTEGER : FactorTemplate.Type.STRING);
                factor.setUnits(input.units);
                return factor;
            }
        };
    }

    private static void addConditionsToLevelsAndRawFiles(ActiveExperiment ex, Iterable<ExperimentSample> samples) {
        new SampleConditionsFactory(ex, ex.rawFiles.getFactors(), samples).create();
    }


}
