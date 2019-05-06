package com.infoclinika.mssharing.platform.model.read;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.model.common.items.NamedItem;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Herman Zamula
 */
public interface DetailsReaderTemplate<
    FILE_ITEM extends DetailsReaderTemplate.FileItemTemplate,
    EXPERIMENT_ITEM extends DetailsReaderTemplate.ExperimentItemTemplate,
    PROJECT_ITEM extends DetailsReaderTemplate.ProjectItemTemplate,
    INSTRUMENT_ITEM extends DetailsReaderTemplate.InstrumentItemTemplate,
    LAB_ITEM extends DetailsReaderTemplate.LabItemTemplateDetailed,
    GROUP_ITEM extends DetailsReaderTemplate.GroupItemTemplate> {

    FILE_ITEM readFile(long actor, long file);

    EXPERIMENT_ITEM readExperiment(long actor, long experiment);

    PROJECT_ITEM readProject(long actor, long project);

    INSTRUMENT_ITEM readInstrument(long actor, long instrument);

    LAB_ITEM readLab(long actor, long lab);

    /**
     * Read details about group
     *
     * @param actor - user ID that requested information
     * @param group - group ID
     * @return group details
     */
    GROUP_ITEM readGroup(long actor, long group);

    /**
     * Used to show experiment details in expand menu on dashboard
     * TODO: Consider move this method and related DTOs to separate service
     *
     * @param actor      current user
     * @param experiment experiment to get details
     * @return experiment item for displaying on dashboard expand menu
     */
    ExperimentShortInfo readExperimentShortInfo(long actor, long experiment);

    enum InstrumentAccess {
        NO_ACCESS, OPERATOR
    }

    class AnnotationItem {
        public final long id;
        public final String name;
        public final String value;
        public final String units;
        public final boolean isNumeric;

        public AnnotationItem(long id, String name, String value, String units, boolean isNumeric) {
            this.id = id;
            this.name = name;
            this.value = value;
            this.units = units;
            this.isNumeric = isNumeric;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AnnotationItem)) {
                return false;
            }
            AnnotationItem that = (AnnotationItem) o;
            return id == that.id &&
                isNumeric == that.isNumeric &&
                Objects.equals(name, that.name) &&
                Objects.equals(value, that.value) &&
                Objects.equals(units, that.units);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, value, units, isNumeric);
        }
    }

    class FileItemTemplate {
        public final long id;
        public final long sizeInBytes;
        public final Date uploadDate;
        public final String labels;
        public final String bucket;
        public final String contentId;
        public final String instrumentName;
        public final String labName;
        public final long instrumentId;
        public final String name;
        public final String owner;
        public final String ownerEmail;
        public final String specieName;
        public final boolean copy;
        public final List<ConditionItem> conditions;
        public final List<AnnotationItem> annotations;

        public FileItemTemplate(long id, long sizeInBytes,
                                Date uploadDate, String labels, String bucket, String contentId,
                                String owner, String ownerEmail, boolean copy, String name, String specieName,
                                String instrumentName,
                                String labName, long instrumentId,
                                List<ConditionItem> conditions,
                                List<AnnotationItem> annotations) {
            this.id = id;
            this.sizeInBytes = sizeInBytes;
            this.uploadDate = uploadDate;
            this.labels = labels;
            this.bucket = bucket;
            this.contentId = contentId;
            this.owner = owner;
            this.ownerEmail = ownerEmail;
            this.copy = copy;
            this.name = name;
            this.specieName = specieName;
            this.instrumentName = instrumentName;
            this.labName = labName;
            this.instrumentId = instrumentId;
            this.conditions = conditions;
            this.annotations = annotations;
        }


        public FileItemTemplate(FileItemTemplate other) {
            this.id = other.id;
            this.sizeInBytes = other.sizeInBytes;
            this.uploadDate = other.uploadDate;
            this.labels = other.labels;
            this.bucket = other.bucket;
            this.contentId = other.contentId;
            this.instrumentName = other.instrumentName;
            this.labName = other.labName;
            this.instrumentId = other.instrumentId;
            this.name = other.name;
            this.owner = other.owner;
            this.ownerEmail = other.ownerEmail;
            this.specieName = other.specieName;
            this.copy = other.copy;
            this.conditions = other.conditions;
            this.annotations = other.annotations;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof FileItemTemplate)) {
                return false;
            }
            FileItemTemplate that = (FileItemTemplate) o;
            return id == that.id &&
                sizeInBytes == that.sizeInBytes &&
                instrumentId == that.instrumentId &&
                copy == that.copy &&
                Objects.equals(uploadDate, that.uploadDate) &&
                Objects.equals(labels, that.labels) &&
                Objects.equals(bucket, that.bucket) &&
                Objects.equals(contentId, that.contentId) &&
                Objects.equals(instrumentName, that.instrumentName) &&
                Objects.equals(labName, that.labName) &&
                Objects.equals(name, that.name) &&
                Objects.equals(owner, that.owner) &&
                Objects.equals(ownerEmail, that.ownerEmail) &&
                Objects.equals(specieName, that.specieName) &&
                Objects.equals(conditions, that.conditions) &&
                Objects.equals(annotations, that.annotations);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                id,
                sizeInBytes,
                uploadDate,
                labels,
                bucket,
                contentId,
                instrumentName,
                labName,
                instrumentId,
                name,
                owner,
                ownerEmail,
                specieName,
                copy,
                conditions,
                annotations
            );
        }
    }

    class ExperimentItemTemplate {
        public final long id;
        public final String name;
        public final long project;
        public final String description;
        public final Date lastModified;
        public final String ownerEmail;
        public final long technologyType;
        public final String instrumentVendor;
        public final long instrumentVendorId;
        public final long instrumentModel;
        public final long instrumentType;
        public final Optional<Long> instrument;
        public final String instrumentName;
        public final Long lab;
        public final String labName;
        //indexed by file/factor indexes in their lists (not by ids). row - files; column - factor
        public final String[][] factorValues;
        public final long experimentType;
        public final long specie;
        public final AccessLevel accessLevel;
        public final Long labHead;
        public final int numberOfRawFiles;
        public final ImmutableList<AttachmentItem> attachments;
        public final ImmutableList<MetaFactorTemplate> factors;
        //TODO: Migrate to separated ExperimentFileItemTemplate for ExperimentItemTemplate DTO
        public final ImmutableList<FileItemTemplate> files;

        public ExperimentItemTemplate(long id,
                                      String name,
                                      long project,
                                      String description,
                                      Date lastModified,
                                      String labName,
                                      int numberOfRawFiles,
                                      ImmutableList<MetaFactorTemplate> factors,
                                      ImmutableList<FileItemTemplate> files,
                                      long specie,
                                      String ownerEmail,
                                      long technologyType,
                                      long experimentType,
                                      Optional<Long> instrument,
                                      String[][] factorValues,
                                      ImmutableList<AttachmentItem> attachments,
                                      String instrumentName,
                                      String instrumentVendor,
                                      long instrumentVendorId,
                                      long instrumentModel,
                                      long instrumentType,
                                      Long lab,
                                      AccessLevel accessLevel,
                                      Long labHead) {
            this.id = id;
            this.name = name;
            this.project = project;
            this.description = description;
            this.lastModified = lastModified;
            this.labName = labName;
            this.numberOfRawFiles = numberOfRawFiles;
            this.factors = factors;
            this.files = files;
            this.technologyType = technologyType;
            this.factorValues = factorValues;
            this.specie = specie;
            this.ownerEmail = ownerEmail;
            this.experimentType = experimentType;
            this.instrument = instrument;
            this.attachments = attachments;
            this.instrumentName = instrumentName;
            this.instrumentVendor = instrumentVendor;
            this.instrumentVendorId = instrumentVendorId;
            this.instrumentModel = instrumentModel;
            this.instrumentType = instrumentType;
            this.lab = lab;
            this.accessLevel = accessLevel;
            this.labHead = labHead;
        }

        @SuppressWarnings("unchecked")
        public ExperimentItemTemplate(ExperimentItemTemplate other) {
            this.id = other.id;
            this.name = other.name;
            this.project = other.project;
            this.description = other.description;
            this.lastModified = other.lastModified;
            this.numberOfRawFiles = other.numberOfRawFiles;
            this.factors = other.factors;
            this.files = other.files;
            this.ownerEmail = other.ownerEmail;
            this.instrumentVendor = other.instrumentVendor;
            this.instrumentModel = other.instrumentModel;
            this.instrument = other.instrument;
            this.instrumentName = other.instrumentName;
            this.lab = other.lab;
            this.factorValues = other.factorValues;
            this.experimentType = other.experimentType;
            this.specie = other.specie;
            this.attachments = other.attachments;
            this.accessLevel = other.accessLevel;
            this.labHead = other.labHead;
            this.labName = other.labName;
            this.instrumentVendorId = other.instrumentVendorId;
            this.technologyType = other.technologyType;
            this.instrumentType = other.instrumentType;
        }
    }

    class ProjectItemTemplate {
        public final long id;
        public final String name;
        public final String description;
        public final Date modified;
        public final Long lab;
        public final Long labHead;
        public final String ownerEmail;
        public final String areaOfResearch;
        public final boolean isPublic;
        public final boolean isPrivate;
        public final int totalSharedMembers;
        public final ImmutableSortedSet<SharedGroup> sharedGroups;
        public final ImmutableSortedSet<SharedPerson> sharedPersons;
        public final ImmutableList<AttachmentItem> attachments;

        public ProjectItemTemplate(long id,
                                   String name,
                                   String description,
                                   Date modified,
                                   boolean isPublic,
                                   ImmutableSortedSet<SharedGroup> sharedGroups,
                                   Long labHead,
                                   String ownerEmail,
                                   boolean isPrivate,
                                   ImmutableList<AttachmentItem> attachments,
                                   int totalSharedMembers,
                                   String areaOfResearch,
                                   ImmutableSortedSet<SharedPerson> sharedPersons,
                                   Long lab) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.modified = modified;
            this.isPublic = isPublic;
            this.sharedGroups = sharedGroups;
            this.labHead = labHead;
            this.ownerEmail = ownerEmail;
            this.isPrivate = isPrivate;
            this.attachments = attachments;
            this.totalSharedMembers = totalSharedMembers;
            this.areaOfResearch = areaOfResearch;
            this.sharedPersons = sharedPersons;
            this.lab = lab;
        }


        public ProjectItemTemplate(ProjectItemTemplate other) {
            this.id = other.id;
            this.name = other.name;
            this.description = other.description;
            this.modified = other.modified;
            this.lab = other.lab;
            this.labHead = other.labHead;
            this.ownerEmail = other.ownerEmail;
            this.areaOfResearch = other.areaOfResearch;
            this.isPublic = other.isPublic;
            this.isPrivate = other.isPrivate;
            this.totalSharedMembers = other.totalSharedMembers;
            this.sharedGroups = other.sharedGroups;
            this.sharedPersons = other.sharedPersons;
            this.attachments = other.attachments;
        }
    }

    class InstrumentItemTemplate {

        public final long id;
        public final String name;
        public final String vendor;
        public final String model;
        public final long modelId;
        public final String serialNumber;
        public final String creator;
        public final String type;
        public final String peripherals;
        public final LabItemTemplate lab;
        public final ImmutableSortedSet<SharedPerson> operators;
        public final InstrumentAccess access;
        public final String studyType;

        public InstrumentItemTemplate(long id,
                                      String name,
                                      String vendor,
                                      String model,
                                      long modelId,
                                      String serialNumber,
                                      String creator,
                                      String peripherals,
                                      ImmutableSortedSet<SharedPerson> operators,
                                      LabItemTemplate lab,
                                      String type,
                                      InstrumentAccess access,
                                      String studyType) {
            this.id = id;
            this.name = name;
            this.vendor = vendor;
            this.model = model;
            this.modelId = modelId;
            this.serialNumber = serialNumber;
            this.creator = creator;
            this.peripherals = peripherals;
            this.operators = operators;
            this.lab = lab;
            this.type = type;
            this.access = access;
            this.studyType = studyType;
        }

        public InstrumentItemTemplate(InstrumentItemTemplate other) {
            this.id = other.id;
            this.name = other.name;
            this.vendor = other.vendor;
            this.model = other.model;
            this.modelId = other.modelId;
            this.serialNumber = other.serialNumber;
            this.creator = other.creator;
            this.type = other.type;
            this.peripherals = other.peripherals;
            this.lab = other.lab;
            this.operators = other.operators;
            this.access = other.access;
            this.studyType = other.studyType;
        }
    }

    class LabItemTemplate {

        public final long id;
        public final String name;
        public final String institutionUrl;
        public final String headFirstName;
        public final String headLastName;
        public final String headEmail;
        public final String contactEmail;
        public final Date modified;

        public LabItemTemplate(long id,
                               String name,
                               String institutionUrl,
                               String headFirstName,
                               String headLastName,
                               String headEmail,
                               String contactEmail,
                               Date modified) {
            this.id = id;
            this.name = name;
            this.institutionUrl = institutionUrl;
            this.headFirstName = headFirstName;
            this.headLastName = headLastName;
            this.headEmail = headEmail;
            this.contactEmail = contactEmail;
            this.modified = modified;
        }
    }

    class LabItemTemplateDetailed extends LabItemTemplate {

        public final long membersCount;

        public LabItemTemplateDetailed(long id,
                                       String name,
                                       String institutionUrl,
                                       String headFirstName,
                                       String headLastName,
                                       String headEmail,
                                       String contactEmail,
                                       Date modified,
                                       long membersCount) {
            super(id, name, institutionUrl, headFirstName, headLastName, headEmail, contactEmail, modified);
            this.membersCount = membersCount;
        }
    }

    /**
     * @author Herman Zamula
     */
    class MetaFactorTemplate {
        public final String name;
        public final String units;
        public final boolean isNumeric;
        public final long id;
        public final long experimentId;

        public MetaFactorTemplate(long id, String name, String units, boolean numeric, long experimentId) {
            this.name = name;
            this.units = units;
            isNumeric = numeric;
            this.id = id;
            this.experimentId = experimentId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MetaFactorTemplate)) {
                return false;
            }
            MetaFactorTemplate that = (MetaFactorTemplate) o;
            return isNumeric == that.isNumeric &&
                id == that.id &&
                experimentId == that.experimentId &&
                Objects.equals(name, that.name) &&
                Objects.equals(units, that.units);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, units, isNumeric, id, experimentId);
        }
    }

    class GroupItemTemplate {
        public final long id;
        public final String name;
        public final Date lastModified;
        public final ImmutableSortedSet<MemberItemTemplate> members;
        public final ImmutableSortedSet<SharedProjectItemTemplate> sharedProjects;

        public GroupItemTemplate(long id,
                                 String name,
                                 Date lastModified,
                                 ImmutableSortedSet<MemberItemTemplate> members,
                                 ImmutableSortedSet<SharedProjectItemTemplate> sharedProjects) {
            this.id = id;
            this.name = name;
            this.lastModified = lastModified;
            this.members = members;
            this.sharedProjects = sharedProjects;
        }
    }

    class SharedProjectItemTemplate {
        public final long id;
        public final String title;

        public SharedProjectItemTemplate(long id, String title) {
            this.id = id;
            this.title = title;
        }
    }

    class MemberItemTemplate extends NamedItem {
        public final String email;

        public MemberItemTemplate(long id, String email, String name) {
            super(id, name);
            this.email = email;
        }
    }

    @Deprecated
    class Annotations {

        @Deprecated
        public final String fractionNumber;
        @Deprecated
        public final String sampleId;

        @Deprecated
        public Annotations(String fractionNumber, String sampleId) {
            this.fractionNumber = fractionNumber;
            this.sampleId = sampleId;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Annotations)) {
                return false;
            }
            Annotations that = (Annotations) o;
            return Objects.equals(fractionNumber, that.fractionNumber) &&
                Objects.equals(sampleId, that.sampleId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fractionNumber, sampleId);
        }
    }

    class AttachmentItem extends NamedItem {
        public final long sizeInBytes;
        public final Date uploadDate;
        public final long ownerId;

        public AttachmentItem(long id, String name, long sizeInBytes, Date uploadDate, long ownerId) {
            super(id, name);
            this.sizeInBytes = sizeInBytes;
            this.uploadDate = uploadDate;
            this.ownerId = ownerId;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AttachmentItem)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            AttachmentItem that = (AttachmentItem) o;
            return sizeInBytes == that.sizeInBytes &&
                ownerId == that.ownerId &&
                Objects.equals(uploadDate, that.uploadDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), sizeInBytes, uploadDate, ownerId);
        }
    }

    class SharedGroup extends NamedItem {
        public final int numberOfMembers;
        public final boolean allowWrite;

        public SharedGroup(long id, String name, int numberOfMembers, boolean allowWrite) {
            super(id, name);
            this.numberOfMembers = numberOfMembers;
            this.allowWrite = allowWrite;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SharedGroup)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            SharedGroup that = (SharedGroup) o;
            return numberOfMembers == that.numberOfMembers &&
                allowWrite == that.allowWrite;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), numberOfMembers, allowWrite);
        }
    }

    final class SharedPerson extends NamedItem {
        public final String email;
        public final boolean allowWrite;

        public SharedPerson(long id, String name, String email, boolean allowWrite) {
            super(id, name);
            this.email = email;
            this.allowWrite = allowWrite;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SharedPerson)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            SharedPerson that = (SharedPerson) o;
            return id == that.id &&
                allowWrite == that.allowWrite &&
                Objects.equals(name, that.name) &&
                Objects.equals(email, that.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), id, name, email, allowWrite);
        }
    }

    class ConditionItem {
        public final long id;
        public final String name;
        public final String experimentName;

        public ConditionItem(long id, String name, String experimentName) {
            this.id = id;
            this.name = name;
            this.experimentName = experimentName;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ConditionItem)) {
                return false;
            }
            ConditionItem that = (ConditionItem) o;
            return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(experimentName, that.experimentName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, experimentName);
        }
    }

    class ExperimentShortInfo {
        public final long id;
        public final String labName;
        public final String name;
        public final String description;
        public final String species;
        public final String projectName;
        public final List<? extends ShortExperimentFileItem> files;
        public final List<AttachmentItem> attachments;
        public final String ownerEmail;

        public ExperimentShortInfo(
            long id,
            String labName,
            String name,
            String description,
            String projectName,
            String specie,
            List<? extends ShortExperimentFileItem> files,
            List<AttachmentItem> attachments,
            String ownerEmail
        ) {
            this.id = id;
            this.labName = labName;
            this.name = name;
            this.description = description;
            this.projectName = projectName;
            this.species = specie;
            this.files = files;
            this.attachments = attachments;
            this.ownerEmail = ownerEmail;
        }
    }

    class ShortExperimentFileItem {
        public final long id;
        public final String name;
        public final ImmutableList<ConditionItem> conditions;
        public final ImmutableList<AnnotationItem> annotations;

        public ShortExperimentFileItem(long id,
                                       String name,
                                       ImmutableList<ConditionItem> conditions,
                                       ImmutableList<AnnotationItem> annotations) {
            this.id = id;
            this.name = name;
            this.conditions = conditions;
            this.annotations = annotations;
        }
    }
}
