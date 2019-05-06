package com.infoclinika.mssharing.model.internal.helper;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.model.helper.ExperimentDownloadHelper;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.helper.items.ChorusExperimentDownloadData;
import com.infoclinika.mssharing.model.helper.items.ChorusFileData;
import com.infoclinika.mssharing.model.helper.items.LockMzData;
import com.infoclinika.mssharing.model.internal.entity.*;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.NgsRelatedData;
import com.infoclinika.mssharing.model.internal.entity.workflow.PrepToExperimentSample;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.InstrumentsDefaults;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate;
import com.infoclinika.mssharing.platform.model.impl.helper.DefaultExperimentDownloadHelper;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.platform.entity.Sharing.*;
import static com.infoclinika.mssharing.platform.entity.Sharing.Type.*;
import static java.util.Optional.*;

@Service("experimentDownloadHelper")
public class ExperimentDownloadHelperImpl extends
    DefaultExperimentDownloadHelper<ExperimentDownloadHelperTemplate.ExperimentItemTemplate,
        ChorusExperimentDownloadData, ChorusFileData>
    implements ExperimentDownloadHelper {

    public static final Function<ExperimentFileTemplate, Long> META_ID_FROM_RAW =
        input -> input.getFileMetaData().getId();

    private static final Function<LockMz, LockMzData> LOCK_MZ_FUNCTION =
        input -> new LockMzData(input.getMass(), input.getCharge());

    private Function<Attachment<User>, AttachmentDataTemplate> attachmentFn(final long creator) {
        return input -> new AttachmentDataTemplate(
            input.getId(),
            input.getName(),
            storedObjectPaths.experimentAttachmentPath(creator, input.getId()).getPath()
        );
    }

    private final Function<ActiveFileMetaData, ChorusFileData> fileMedataTransformer =
        new Function<ActiveFileMetaData, ChorusFileData>() {
            @Override
            public ChorusFileData apply(ActiveFileMetaData input) {
                final Type exptSharingType = fileMetaDataRepository.getSharingTypeThroughExperiment(input.getId());
                final Type sharingType = ofNullable(exptSharingType).orElse(PRIVATE);

                return new ChorusFileData(
                    input.getBucket(),
                    input.getContentId(),
                    input.getArchiveId(),
                    input.getName(),
                    input.isInvalid(),
                    null,
                    input.getInstrument().getLab().getId(),
                    input.getId(),
                    fromNullable(input.getBillLab()).transform(EntityUtil.ENTITY_TO_ID),
                    Transformers.fromSharingType(sharingType),
                    input.getInstrument().getName()
                );
            }
        };

    @Inject
    Transformers transformers;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    private StoredObjectPaths storedObjectPaths;


    @Override
    protected String getPublicDownloadLink(ExperimentTemplate experimentTemplate) {
        return transformers.getPublicDownloadLink((ActiveExperiment) experimentTemplate);
    }

    @Override
    protected String getPrivateDownloadLink(ExperimentTemplate experimentTemplate) {
        return transformers.getPrivateDownloadLink((ActiveExperiment) experimentTemplate);
    }

    @Override
    protected ChorusFileData transformFileData(FileMetaDataTemplate metaDataTemplate) {
        return fileMedataTransformer.apply((ActiveFileMetaData) metaDataTemplate);
    }

    @Override
    protected ChorusExperimentDownloadData transformExperimentDownloadData(ExperimentTemplate experimentTemplate) {
        final ActiveExperiment experiment = (ActiveExperiment) experimentTemplate;
        final String experimentName = experiment.getName();

        final List<AttachmentDataTemplate> attachments = new ArrayList<>(
            Lists.transform(experiment.attachments, attachmentFn(experiment.getCreator().getId())));
        final Sharing sharing = experiment.getProject().getSharing();

        final List<FileDataTemplate> files = experiment.getRawFiles()
            .getData()
            .stream()
            .map(input -> getChorusFileData(experimentName, sharing, input))
            .collect(Collectors.toList());

        ChorusExperimentDownloadData experimentDownloadData = new ChorusExperimentDownloadData(attachments, files);
        experimentDownloadData.add(ChorusExperimentDownloadData.EXPERIMENT_NAME, experimentName);
        experimentDownloadData.add("Project", experiment.getProject().getName());
        experimentDownloadData.add("Specie", experiment.getSpecie().getName());
        experimentDownloadData.add("Experiment Type", getExperimentType(experiment));
        experimentDownloadData.add("Instrument", getInstrumentName(experiment));
        experimentDownloadData.add("Description", experiment.getExperiment().getDescription());

        switch (experiment.getInstrumentRestriction().getInstrumentModel().getStudyType().getName()) {
            case InstrumentsDefaults.NG_TECHNOLOGY_TYPE:
                NgsRelatedData ngsRelatedData = experiment.getNgsRelatedData();
                experimentDownloadData.add("Xenograft", getYesOrNo(ngsRelatedData.isXenograft()));
                experimentDownloadData.add("Multiplexing", getYesOrNo(ngsRelatedData.isMultiplexing()));
                if (ngsRelatedData.isMultiplexing()) {
                    experimentDownloadData.add(
                        "Number of Samples",
                        String.valueOf(experiment.getRawFiles().getData().size())
                    );
                }
                if (ngsRelatedData.getPairedEnd() != 0) {
                    experimentDownloadData.add(
                        "Paired End",
                        ngsRelatedData.getPairedEnd() == 1 ? "Single File" : "Two Files"
                    );
                }
                if (experiment.getNgsRelatedData().getExperimentPrepMethod() != null) {
                    experimentDownloadData.add(
                        "Experiment Prep Method",
                        ngsRelatedData.getExperimentPrepMethod().getTitle()
                    );
                }
                experimentDownloadData.add("Library Prep", ngsRelatedData.getLibraryPrep().getTitle());
                break;
            case InstrumentsDefaults.MS_TECHNOLOGY_TYPE:
                final List<LockMzData> lockMasses = newArrayList();
                lockMasses.addAll(Lists.transform(experiment.getLockMasses(), LOCK_MZ_FUNCTION));
                experimentDownloadData.add("2D/LC", getEnabled2dLc(experiment));
                experimentDownloadData.add("Lock m/z", lockMasses.stream().map(LockMzData::toString)
                    .collect(Collectors.joining(", ")));
                break;
            case InstrumentsDefaults.MA_TECHNOLOGY_TYPE:
                break;
            case InstrumentsDefaults.CL_TECHNOLOGY_TYPE:
                break;
            default:
                throw new RuntimeException("Unknown experiment type {"
                    + experiment.getInstrumentRestriction().getInstrumentModel().getStudyType().getName() + "}");
        }

        return experimentDownloadData;
    }

    private String getExperimentType(ActiveExperiment experiment) {
        if (experiment.getInstrumentRestriction().getInstrumentModel().getStudyType().getName()
            .equalsIgnoreCase(InstrumentsDefaults.NG_TECHNOLOGY_TYPE)) {
            return experiment.getNgsRelatedData().getNgsExperimentTypeName();
        }
        ExperimentType exType = experiment.getExperimentType();
        return exType != null ? exType.getName() : null;
    }

    private String getEnabled2dLc(ActiveExperiment experiment) {
        ExperimentType exType = experiment.getExperimentType();
        boolean allow2dLc = exType != null && exType.allowed2dLC;
        return getYesOrNo(allow2dLc && experiment.getExperiment().is2dLc());
    }

    private String getInstrumentName(ActiveExperiment experiment) {
        Instrument instrument = experiment.getInstrumentRestriction().getInstrument();
        return instrument != null ? instrument.getName() : "All";
    }

    private String getYesOrNo(boolean value) {
        return value ? "Yes" : "No";
    }

    private ChorusFileData getChorusFileData(String experimentName, Sharing sharing, RawFile input) {
        //noinspection unchecked
        final Set<PrepToExperimentSample> samples = input.getPreparedSample().getSamples();
        final Set<SampleCondition> fileConditions = newHashSet();
        for (PrepToExperimentSample prepToExperimentSample : samples) {
            fileConditions.addAll(prepToExperimentSample.getExperimentSample().getSampleConditions());
        }
        final List<ConditionDataTemplate> conditions = fileConditions.stream()
            .map(input1 -> new ConditionDataTemplate(input1.getId(), input1.getName(), experimentName))
            .collect(Collectors.toList());

        final AbstractFileMetaData fileMetaData = input.getFileMetaData();
        return new ChorusFileData(
            fileMetaData.getBucket(),
            fileMetaData.getContentId(),
            fileMetaData.getArchiveId(),
            fileMetaData.getName(),
            fileMetaData.isInvalid(),
            conditions,
            fileMetaData.getInstrument().getLab().getId(),
            fileMetaData.getId(),
            fromNullable(fileMetaData.getBillLab()).transform(EntityUtil.ENTITY_TO_ID),
            Transformers.fromSharingType(sharing.getType()),
            fileMetaData.getInstrument().getName()
        );
    }

    @Override
    protected ExperimentItemTemplate transformExperimentItem(ExperimentTemplate experiment) {

        //noinspection unchecked
        return new ExperimentItemTemplate(experiment.getCreator().getId(), experiment.getId(),
            from(experiment.getRawFiles().getData())
                .transform(META_ID_FROM_RAW).toSet()
        );
    }
}
