package com.infoclinika.mssharing.platform.model;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.impl.entities.*;
import com.infoclinika.mssharing.platform.model.impl.entities.restorable.ExperimentDefault;
import com.infoclinika.mssharing.platform.model.impl.entities.restorable.FileMetaDataDefault;
import com.infoclinika.mssharing.platform.model.impl.entities.restorable.ProjectDefault;

import java.util.Optional;

import static com.google.common.base.Optional.fromNullable;

/**
 * @author Herman Zamula
 */
public final class EntityFactories {


    public final Supplier<InstrumentCreationRequestTemplate> defaultInstrumentRequest =
        () -> new InstrumentCreationRequestDefault();
    public final Supplier<ProjectTemplate> defaultProject = () -> new ProjectDefault();
    public final Supplier<ExperimentTemplate> defaultExperiment = () -> new ExperimentDefault();
    public final Supplier<FileMetaDataTemplate> defaultFileMetaData = () -> new FileMetaDataDefault();
    public final Supplier<ExperimentFileTemplate> defaultRawFile = () -> new ExperimentFileDefault();
    public final Supplier<InstrumentTemplate> defaultInstrument = () -> new InstrumentDefault();
    public final Supplier<FactorTemplate> defaultFactor = () -> new FactorDefault();
    public final Supplier<LevelTemplate> defaultLevel = () -> new LevelDefault();
    public final Supplier<UserTemplate> defaultUser = () -> new UserDefault();
    public final Supplier<ProjectSharingRequestTemplate> defaultProjectSharingRequest =
        () -> new ProjectSharingRequestTemplate();
    public final Supplier<GroupTemplate> defaultGroup = () -> new GroupDefault();
    public final Supplier<LabTemplate> defaultLab = () -> new LabDefault();
    public final Supplier<InboxMessageTemplate> defaultInboxMessage = () -> new InboxMessageTemplate();
    public final Supplier<OutboxMessageTemplate> defaultOutboxMessage = () -> new OutboxMessageTemplate();
    public final Supplier<Attachment> defaultAttachment = () -> new Attachment();
    public final Supplier<Species> defaultSpecies = () -> new Species();
    public final Supplier<ExperimentType> defaultExperimentType = () -> new ExperimentType();
    public final Supplier<InstrumentModel> defaultInstrumentModel = () -> new InstrumentModel();
    public final Supplier<InstrumentType> defaultInstrumentType = () -> new InstrumentType();
    public final Supplier<InstrumentStudyType> defaultInstrumentStudyType = InstrumentStudyType::new;
    public final Supplier<VendorExtension> defaultVendorExtension = VendorExtension::new;
    public final Supplier<Vendor> defaultVendor = () -> new Vendor();
    public final Supplier<AnnotationTemplate> defaultAnnotation = () -> new AnnotationDefault();

    public final Supplier<InstrumentCreationRequestTemplate> instrumentRequest;
    public final Supplier<ProjectTemplate> project;
    public final Supplier<ExperimentTemplate> experiment;
    public final Supplier<FileMetaDataTemplate> fileMetaData;
    public final Supplier<ExperimentFileTemplate> rawFile;
    public final Supplier<InstrumentTemplate> instrument;
    public final Supplier<FactorTemplate> factor;
    public final Supplier<LevelTemplate> level;
    public final Supplier<UserTemplate> user;
    public final Supplier<ProjectSharingRequestTemplate> projectSharingRequest;
    public final Supplier<GroupTemplate> group;
    public final Supplier<LabTemplate> lab;
    public final Supplier<InboxMessageTemplate> inboxMessage;
    public final Supplier<OutboxMessageTemplate> outboxMessage;
    public final Supplier<Attachment> attachment;
    public final Supplier<Species> species;
    public final Supplier<ExperimentType> experimentType;
    public final Supplier<InstrumentModel> instrumentModel;
    public final Supplier<InstrumentType> instrumentType;
    public final Supplier<InstrumentStudyType> instrumentStudyType;
    public final Supplier<VendorExtension> vendorExtension;
    public final Supplier<Vendor> vendor;
    public final Supplier<AnnotationTemplate> annotation;


    public final Function<Long, ? extends UserTemplate> userFromId = new Function<Long, UserTemplate>() {
        @Override
        public UserTemplate apply(Long input) {
            return fromId(user, input);
        }
    };

    public final Function<Long, ? extends GroupTemplate> groupFromId = new Function<Long, GroupTemplate>() {
        @Override
        public GroupTemplate apply(Long input) {
            return fromId(group, input);
        }
    };

    public final Function<Long, InstrumentTemplate> instrumentFromId = new Function<Long, InstrumentTemplate>() {
        @Override
        public InstrumentTemplate apply(Long input) {
            return fromId(instrument, input);
        }
    };

    public final Function<Long, InstrumentModel> instrumentModelFromId = new Function<Long, InstrumentModel>() {
        @Override
        public InstrumentModel apply(Long input) {
            return fromId(instrumentModel, input);
        }
    };

    public final Function<Long, Species> speciesFromId = new Function<Long, Species>() {
        @Override
        public Species apply(Long input) {
            return fromId(species, input);
        }
    };

    public final Function<Long, LabTemplate> labFromId = new Function<Long, LabTemplate>() {
        @Override
        public LabTemplate apply(Long input) {
            return fromId(lab, input);
        }
    };

    public final Function<Long, ProjectTemplate> projectFromId = new Function<Long, ProjectTemplate>() {
        @Override
        public ProjectTemplate apply(Long input) {
            return fromId(project, input);
        }
    };

    public final Function<Long, FileMetaDataTemplate> fileFromId = new Function<Long, FileMetaDataTemplate>() {
        @Override
        public FileMetaDataTemplate apply(Long input) {
            return fromId(fileMetaData, input);
        }
    };

    public final Function<Long, ExperimentType> experimentTypeFromId = new Function<Long, ExperimentType>() {
        @Override
        public ExperimentType apply(Long input) {
            return fromId(experimentType, input);
        }
    };

    public EntityFactories(
        Supplier<UserTemplate> user,
        Supplier<InstrumentCreationRequestTemplate> instrumentRequest,
        Supplier<ProjectTemplate> project,
        Supplier<ExperimentTemplate> experiment,
        Supplier<FileMetaDataTemplate> fileMetaData,
        Supplier<ExperimentFileTemplate> rawFile,
        Supplier<InstrumentTemplate> instrument,
        Supplier<FactorTemplate> factor,
        Supplier<LevelTemplate> level,
        Supplier<ProjectSharingRequestTemplate> projectSharingRequest,
        Supplier<GroupTemplate> group,
        Supplier<LabTemplate> lab,
        Supplier<InboxMessageTemplate> inboxMessage,
        Supplier<OutboxMessageTemplate> outboxMessage,
        Supplier<Attachment> attachment, Supplier<Species> species,
        Supplier<ExperimentType> experimentType,
        Supplier<InstrumentModel> instrumentModel,
        Supplier<InstrumentType> instrumentType,
        Supplier<InstrumentStudyType> instrumentStudyType,
        Supplier<VendorExtension> vendorExtension,
        Supplier<Vendor> vendor,
        Supplier<AnnotationTemplate> annotation) {

        this.annotation = fromNullable(annotation).or(defaultAnnotation);

        this.instrumentModel = fromNullable(instrumentModel).or(defaultInstrumentModel);
        this.instrumentType = fromNullable(instrumentType).or(defaultInstrumentType);
        this.instrumentStudyType = fromNullable(instrumentStudyType).or(defaultInstrumentStudyType);
        this.vendorExtension = Optional.ofNullable(vendorExtension).orElse(defaultVendorExtension);
        this.vendor = fromNullable(vendor).or(defaultVendor);

        this.experimentType = fromNullable(experimentType).or(defaultExperimentType);

        this.species = fromNullable(species).or(defaultSpecies);

        this.user = fromNullable(user).or(defaultUser);
        this.instrumentRequest = fromNullable(instrumentRequest).or(defaultInstrumentRequest);

        this.project = fromNullable(project).or(defaultProject);
        this.experiment = fromNullable(experiment).or(defaultExperiment);
        this.fileMetaData = fromNullable(fileMetaData).or(defaultFileMetaData);
        this.rawFile = fromNullable(rawFile).or(defaultRawFile);
        this.instrument = fromNullable(instrument).or(defaultInstrument);
        this.factor = fromNullable(factor).or(defaultFactor);
        this.level = fromNullable(level).or(defaultLevel);
        this.projectSharingRequest = fromNullable(projectSharingRequest).or(defaultProjectSharingRequest);
        this.group = fromNullable(group).or(defaultGroup);
        this.lab = fromNullable(lab).or(defaultLab);
        this.inboxMessage = fromNullable(inboxMessage).or(defaultInboxMessage);
        this.outboxMessage = fromNullable(outboxMessage).or(defaultOutboxMessage);
        this.attachment = fromNullable(attachment).or(defaultAttachment);
    }

    private <T extends AbstractPersistable> T fromId(Supplier<T> factory, long id) {
        final T entity = factory.get();
        entity.setId(id);
        return entity;
    }

    public static class Builder {

        private Supplier<UserTemplate> user;
        private Supplier<InstrumentCreationRequestTemplate> instrumentRequest;
        private Supplier<ProjectTemplate> project;
        private Supplier<ExperimentTemplate> experiment;
        private Supplier<FileMetaDataTemplate> fileMetaData;
        private Supplier<ExperimentFileTemplate> rawFile;
        private Supplier<InstrumentTemplate> instrument;
        private Supplier<FactorTemplate> factor;
        private Supplier<LevelTemplate> level;
        private Supplier<ProjectSharingRequestTemplate> projectSharingRequest;
        private Supplier<GroupTemplate> group;
        private Supplier<LabTemplate> lab;
        private Supplier<InboxMessageTemplate> inboxMessage;
        private Supplier<OutboxMessageTemplate> outboxMessage;
        private Supplier<Attachment> attachment;
        private Supplier<Species> species;
        private Supplier<ExperimentType> experimentType;
        private Supplier<InstrumentModel> instrumentModel;
        private Supplier<InstrumentType> instrumentType;
        private Supplier<InstrumentStudyType> instrumentStudyType;
        private Supplier<VendorExtension> vendorExtension;
        private Supplier<Vendor> vendor;
        private Supplier<AnnotationTemplate> annotation;


        public Builder user(Supplier<UserTemplate> user) {
            this.user = user;
            return this;
        }

        public Builder instrumentRequest(Supplier<InstrumentCreationRequestTemplate> instrumentRequest) {
            this.instrumentRequest = instrumentRequest;
            return this;
        }

        public Builder project(Supplier<ProjectTemplate> project) {
            this.project = project;
            return this;
        }

        public Builder experiment(Supplier<ExperimentTemplate> experiment) {
            this.experiment = experiment;
            return this;
        }

        public Builder fileMetaData(Supplier<FileMetaDataTemplate> fileMetaData) {
            this.fileMetaData = fileMetaData;
            return this;
        }

        public Builder rawFile(Supplier<ExperimentFileTemplate> rawFile) {
            this.rawFile = rawFile;
            return this;
        }

        public Builder instrument(Supplier<InstrumentTemplate> instrument) {
            this.instrument = instrument;
            return this;
        }

        public Builder factor(Supplier<FactorTemplate> factor) {
            this.factor = factor;
            return this;
        }

        public Builder level(Supplier<LevelTemplate> level) {
            this.level = level;
            return this;
        }

        public Builder projectSharingRequest(Supplier<ProjectSharingRequestTemplate> projectSharingRequest) {
            this.projectSharingRequest = projectSharingRequest;
            return this;
        }

        public Builder group(Supplier<GroupTemplate> group) {
            this.group = group;
            return this;
        }

        public Builder lab(Supplier<LabTemplate> lab) {
            this.lab = lab;
            return this;
        }

        public Builder inboxMessage(Supplier<InboxMessageTemplate> inboxMessage) {
            this.inboxMessage = inboxMessage;
            return this;
        }

        public Builder outboxMessage(Supplier<OutboxMessageTemplate> outboxMessage) {
            this.outboxMessage = outboxMessage;
            return this;
        }

        public Builder attachment(Supplier<Attachment> attachment) {
            this.attachment = attachment;
            return this;
        }

        public Builder species(Supplier<Species> species) {
            this.species = species;
            return this;
        }

        public Builder experimentType(Supplier<ExperimentType> experimentType) {
            this.experimentType = experimentType;
            return this;
        }

        public Builder vendor(Supplier<Vendor> vendor) {
            this.vendor = vendor;
            return this;
        }

        public Builder instrumentType(Supplier<InstrumentType> instrumentType) {
            this.instrumentType = instrumentType;
            return this;
        }

        public Builder instrumentStudyType(Supplier<InstrumentStudyType> instrumentStudyType) {
            this.instrumentStudyType = instrumentStudyType;
            return this;
        }

        public Builder vendorExtension(Supplier<VendorExtension> vendorExtension) {
            this.vendorExtension = vendorExtension;
            return this;
        }

        public Builder instrumentModel(Supplier<InstrumentModel> instrumentModel) {
            this.instrumentModel = instrumentModel;
            return this;
        }

        public Builder annotation(Supplier<AnnotationTemplate> annotation) {
            this.annotation = annotation;
            return this;
        }

        public EntityFactories build() {
            return new EntityFactories(
                user,
                instrumentRequest,
                project,
                experiment,
                fileMetaData,
                rawFile,
                instrument,
                factor,
                level,
                projectSharingRequest,
                group,
                lab,
                inboxMessage,
                outboxMessage,
                attachment,
                species,
                experimentType,
                instrumentModel,
                instrumentType,
                instrumentStudyType,
                vendorExtension,
                vendor,
                annotation
            );

        }


    }
}
