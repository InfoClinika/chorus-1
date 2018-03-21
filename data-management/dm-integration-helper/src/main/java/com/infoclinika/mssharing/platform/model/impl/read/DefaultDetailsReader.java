package com.infoclinika.mssharing.platform.model.impl.read;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.helper.read.SingleResultBuilder;
import com.infoclinika.mssharing.platform.model.helper.read.details.*;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.repository.*;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate.AccessedInstrument;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkAccess;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;


/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultDetailsReader<
        FILE extends FileMetaDataTemplate,
        PROJECT extends ProjectTemplate,
        EXPERIMENT extends ExperimentTemplate,
        INSTRUMENT extends InstrumentTemplate,
        LAB extends LabTemplate,
        GROUP extends GroupTemplate,
        FILE_ITEM extends DetailsReaderTemplate.FileItemTemplate,
        EXPERIMENT_ITEM extends DetailsReaderTemplate.ExperimentItemTemplate,
        PROJECT_ITEM extends DetailsReaderTemplate.ProjectItemTemplate,
        INSTRUMENT_ITEM extends DetailsReaderTemplate.InstrumentItemTemplate,
        LAB_ITEM extends DetailsReaderTemplate.LabItemTemplateDetailed,
        GROUP_ITEM extends DetailsReaderTemplate.GroupItemTemplate>

        implements DetailsReaderTemplate<FILE_ITEM, EXPERIMENT_ITEM, PROJECT_ITEM, INSTRUMENT_ITEM, LAB_ITEM, GROUP_ITEM> {

    @Inject
    protected RuleValidator ruleValidator;
    @Inject
    protected FileDetailsReaderHelper<FILE, FILE_ITEM> fileHelper;
    @Inject
    protected ProjectDetailsReaderHelper<PROJECT, PROJECT_ITEM> projectHelper;
    @Inject
    protected InstrumentDetailsReaderHelper<INSTRUMENT, INSTRUMENT_ITEM> instrumentHelper;
    @Inject
    protected LabDetailsReaderHelper<LAB, LAB_ITEM> labHelper;
    @Inject
    protected GroupDetailsReaderHelper<GROUP, GROUP_ITEM> groupHelper;
    @Inject
    protected ExperimentDetailsReaderHelper<EXPERIMENT, EXPERIMENT_ITEM> experimentHelper;
    @Inject
    private FileRepositoryTemplate<FILE> fileMetaDataRepository;
    @Inject
    private ProjectRepositoryTemplate<PROJECT> projectRepository;
    @Inject
    private InstrumentRepositoryTemplate<INSTRUMENT> instrumentRepository;
    @Inject
    private GroupRepositoryTemplate<GROUP> groupRepository;
    @Inject
    private ExperimentRepositoryTemplate<EXPERIMENT> experimentRepository;
    @Inject
    private DetailsTransformersTemplate detailsTransformers;


    @PostConstruct
    private void init() {

        fileHelper.setTransformer(new Function<FILE, FILE_ITEM>() {
            @Override
            public FILE_ITEM apply(FILE input) {
                return transformFile(input);
            }
        });
        projectHelper.setTransformer(new Function<PROJECT, PROJECT_ITEM>() {
            @Override
            public PROJECT_ITEM apply(PROJECT input) {
                return transformProject(input);
            }
        });
        instrumentHelper.setTransformer(new Function<AccessedInstrument<INSTRUMENT>, INSTRUMENT_ITEM>() {
            @Override
            public INSTRUMENT_ITEM apply(AccessedInstrument<INSTRUMENT> input) {
                return transformInstrument(input);
            }
        });
        labHelper.setTransformer(new Function<LAB, LAB_ITEM>() {
            @Override
            public LAB_ITEM apply(LAB input) {
                return transformLab(input);
            }
        });
        groupHelper.setTransformer(new Function<GROUP, GROUP_ITEM>() {
            @Override
            public GROUP_ITEM apply(GROUP input) {
                return transformGroup(input);
            }
        });
        experimentHelper.setTransformer(new Function<EXPERIMENT, EXPERIMENT_ITEM>() {
            @Override
            public EXPERIMENT_ITEM apply(EXPERIMENT input) {
                return transformExperiment(input);
            }
        });

    }


    @Override
    public FILE_ITEM readFile(long actor, long file) {

        beforeReadFile(actor, file);
        final SingleResultBuilder<FILE, FILE_ITEM> fileItemBuilder = fileHelper.readFile(file);
        return afterReadFile(actor, fileItemBuilder);

    }

    @Override
    public EXPERIMENT_ITEM readExperiment(long actor, long experiment) {

        beforeReadExperiment(actor, experiment);
        final SingleResultBuilder<EXPERIMENT, EXPERIMENT_ITEM> resultBuilder = experimentHelper.readExperiment(experiment);
        return afterReadExperiment(actor, resultBuilder);

    }

    @Override
    public PROJECT_ITEM readProject(long actor, long project) {

        beforeReadProject(actor, project);
        final SingleResultBuilder<PROJECT, PROJECT_ITEM> projectItemBuilder = projectHelper.readProject(project);
        return afterReadProject(actor, projectItemBuilder);

    }

    @Override
    public INSTRUMENT_ITEM readInstrument(long actor, long instrument) {

        beforeReadInstrument(actor, instrument);
        final SingleResultBuilder<AccessedInstrument<INSTRUMENT>, INSTRUMENT_ITEM> builder = instrumentHelper.readInstrument(actor, instrument);
        return afterReadInstrument(actor, builder);

    }

    @Override
    public LAB_ITEM readLab(long actor, long lab) {

        beforeReadLab(actor, lab);
        final SingleResultBuilder<LAB, LAB_ITEM> labBuilder = labHelper.readLab(lab);
        return afterReadLab(actor, labBuilder);

    }

    @Override
    public GROUP_ITEM readGroup(long actor, long group) {

        beforeReadGroup(actor, group);
        final SingleResultBuilder<GROUP, GROUP_ITEM> groupItemBuilder = groupHelper.readGroup(group);
        return afterReadGroup(actor, groupItemBuilder);

    }

    @Override
    public ExperimentShortInfo readExperimentShortInfo(long actor, final long experimentId) {
        beforeReadExperiment(actor, experimentId);

        final EXPERIMENT experiment = experimentRepository.findOne(experimentId);

        //noinspection unchecked
        final List<ShortExperimentFileItem> files = from(experiment.getRawFiles().getData()).transform(shortInfoFileTransformer(experiment)).toList();

        //noinspection unchecked
        final List<AttachmentItem> attachments = from(experiment.attachments).transform(detailsTransformers.attachmentTransformer()).toList();

        final LabTemplate lab = experiment.getLab();

        return new ExperimentShortInfo(experimentId,
                lab == null ? null : lab.getName(),
                experiment.getName(),
                experiment.getExperiment().getDescription(),
                experiment.getProject().getName(),
                experiment.getSpecie().getName(),
                files,
                attachments,
                experiment.getCreator().getEmail());
    }

    protected Function<ExperimentFileTemplate, ? extends ShortExperimentFileItem> shortInfoFileTransformer(final EXPERIMENT experiment) {
        return new Function<ExperimentFileTemplate, ShortExperimentFileItem>() {
            @Override
            public ShortExperimentFileItem apply(ExperimentFileTemplate input) {

                //noinspection unchecked
                final ImmutableList<ConditionItem> conditions = from(input.getConditions()).transform(shortInfoConditionTransformer(experiment)).toList();

                //noinspection unchecked
                final ImmutableList<DetailsReaderTemplate.AnnotationItem> annotations = from(input.getAnnotationList())
                        .transform(detailsTransformers.annotationsTransformer()).toList();

                final FileMetaDataTemplate fileMetaData = input.getFileMetaData();

                return new ShortExperimentFileItem(fileMetaData.getId(),
                        fileMetaData.getName(),
                        conditions,
                        annotations);
            }
        };
    }

    protected Function<Condition, ConditionItem> shortInfoConditionTransformer(final EXPERIMENT experiment) {
        return new Function<Condition, ConditionItem>() {
            @Override
            public ConditionItem apply(Condition input) {
                return new ConditionItem(input.getId(), input.getName(), experiment.getName());
            }
        };
    }

    /*----------------------------- Before read extension points -----------------------------------------------------*/

    protected void beforeReadFile(long actor, long file) {

        checkPresence(fileMetaDataRepository.findOne(file));

        if (!ruleValidator.userHasReadPermissionsOnFile(actor, file)) {
            throw new AccessDenied("User has no permissions to read file");
        }

    }

    protected void beforeReadProject(long actor, long project) {

        checkPresence(projectRepository.findOne(project));

        if (!ruleValidator.hasReadAccessOnProject(actor, project)) {
            throw new AccessDenied("Project read restricted");
        }
    }

    protected void beforeReadGroup(long actor, long group) {

        checkPresence(groupRepository.findOne(group), "Group not found");

        if (!ruleValidator.canReadGroupDetails(actor, group)) throw new AccessDenied("Can't read group");
    }

    protected void beforeReadInstrument(long actor, long instrument) {

        checkPresence(instrumentRepository.findOne(instrument), "Instrument not found");

        if (!ruleValidator.isUserCanReadInstrument(actor, instrument)) {
            throw new AccessDenied("Can't read instrument");
        }

    }

    protected void beforeReadExperiment(long actor, long experiment) {

        checkPresence(experimentRepository.findOne(experiment),
                "Experiment not found. Id=" + experiment + ". User=" + actor);

        checkAccess(ruleValidator.isUserCanReadExperiment(actor, experiment),
                "Can't read experiment. Id=" + experiment + ". User=" + actor);

    }

    /*------------------------- After read extension points ----------------------------------------------------------*/

    protected LAB_ITEM afterReadLab(long actor, SingleResultBuilder<LAB, LAB_ITEM> labBuilder) {
        return labBuilder.transform();
    }

    protected EXPERIMENT_ITEM afterReadExperiment(long actor, SingleResultBuilder<EXPERIMENT, EXPERIMENT_ITEM> resultBuilder) {
        return resultBuilder.transform();
    }

    protected void beforeReadLab(long actor, long lab) {
        if (!ruleValidator.canReadLabs(actor)) {
            throw new AccessDenied("User should be admin to read lab details");
        }
    }

    protected FILE_ITEM afterReadFile(long actor, SingleResultBuilder<FILE, FILE_ITEM> fileItemBuilder) {
        return fileItemBuilder.transform();
    }

    protected PROJECT_ITEM afterReadProject(long actor, SingleResultBuilder<PROJECT, PROJECT_ITEM> projectItemBuilder) {
        return projectItemBuilder.transform();
    }

    protected INSTRUMENT_ITEM afterReadInstrument(long actor, SingleResultBuilder<AccessedInstrument<INSTRUMENT>, INSTRUMENT_ITEM> instrumentItemBuilder) {
        return instrumentItemBuilder.transform();
    }

    protected GROUP_ITEM afterReadGroup(long actor, SingleResultBuilder<GROUP, GROUP_ITEM> groupItemBuilder) {
        return groupItemBuilder.transform();
    }

    public abstract FILE_ITEM transformFile(FILE file);

    public abstract PROJECT_ITEM transformProject(PROJECT project);

    public abstract EXPERIMENT_ITEM transformExperiment(EXPERIMENT experiment);

    public abstract INSTRUMENT_ITEM transformInstrument(AccessedInstrument<INSTRUMENT> instrument);

    public abstract LAB_ITEM transformLab(LAB lab);

    public abstract GROUP_ITEM transformGroup(GROUP group);

}
