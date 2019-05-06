package com.infoclinika.mssharing.platform.model.read;

import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.common.items.FileItem;

import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Herman Zamula
 */
public interface FileReaderTemplate<FILE_LINE extends FileReaderTemplate.FileLineTemplate> {

    Set<FILE_LINE> readFiles(long actor, Filter genericFilter);

    PagedItem<FILE_LINE> readFiles(long actor, Filter genericFilter, PagedItemInfo pagedItemInfo);

    Set<FILE_LINE> readUnfinishedFiles(long user);

    Set<FILE_LINE> readFilesByInstrument(long actor, long instrument);

    PagedItem<FILE_LINE> readFilesByInstrument(long actor, long instrument, PagedItemInfo pagedInfo);

    Set<FILE_LINE> readByNameForInstrument(long actor, long instrument, String fileName);

    Set<FILE_LINE> readFilesByLab(long actor, long lab);

    PagedItem<FILE_LINE> readFilesByLab(long actor, long lab, PagedItemInfo pagedInfo);

    Set<FILE_LINE> readFilesByExperiment(long actor, long experiment);

    PagedItem<FILE_LINE> readFilesByExperiment(long actor, long experiment, PagedItemInfo pagedInfo);

    SortedSet<FileItem> readFileItemsByExperiment(long actor, long experiment);

    class FileLineTemplate {

        public final long id;
        public final String name;
        public final String contentId;
        public final String uploadId;
        public final String destinationPath;
        public final long instrumentId;
        public final String instrumentName;
        public final long modelId;
        public final String instrumentModel;
        public final String labName;
        public final long labId;
        public final long labHead;
        public final Long specieId;
        public final long owner;
        public final boolean invalid;
        public final String vendorName;
        public final AccessLevel accessLevel;
        public final boolean usedInExperiments;
        public final String labels;
        public final long sizeInBytes;
        public final Date uploadDate;

        public FileLineTemplate(long id,
                                String name,
                                String contentId,
                                String uploadId,
                                String destinationPath,
                                long instrumentId,
                                long labId,
                                String instrumentName,
                                long modelId,
                                String labName,
                                long owner,
                                long labHead,
                                boolean invalid,
                                String vendorName,
                                String instrumentModel,
                                Long specieId,
                                AccessLevel accessLevel,
                                boolean usedInExperiments,
                                String labels,
                                long sizeInBytes,
                                Date uploadDate) {
            this.id = id;
            this.name = name;
            this.contentId = contentId;
            this.uploadId = uploadId;
            this.destinationPath = destinationPath;
            this.instrumentId = instrumentId;
            this.labId = labId;
            this.instrumentName = instrumentName;
            this.modelId = modelId;
            this.labName = labName;
            this.owner = owner;
            this.labHead = labHead;
            this.invalid = invalid;
            this.vendorName = vendorName;
            this.instrumentModel = instrumentModel;
            this.specieId = specieId;
            this.accessLevel = accessLevel;
            this.usedInExperiments = usedInExperiments;
            this.labels = labels;
            this.sizeInBytes = sizeInBytes;
            this.uploadDate = uploadDate;
        }

        public FileLineTemplate(FileLineTemplate other) {
            this.id = other.id;
            this.name = other.name;
            this.contentId = other.contentId;
            this.uploadId = other.uploadId;
            this.destinationPath = other.destinationPath;
            this.instrumentId = other.instrumentId;
            this.instrumentName = other.instrumentName;
            this.modelId = other.modelId;
            this.instrumentModel = other.instrumentModel;
            this.labId = other.labId;
            this.labHead = other.labHead;
            this.specieId = other.specieId;
            this.owner = other.owner;
            this.invalid = other.invalid;
            this.vendorName = other.vendorName;
            this.accessLevel = other.accessLevel;
            this.usedInExperiments = other.usedInExperiments;
            this.sizeInBytes = other.sizeInBytes;
            this.labName = other.labName;
            this.labels = other.labels;
            this.uploadDate = other.uploadDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof FileLineTemplate)) {
                return false;
            }
            FileLineTemplate that = (FileLineTemplate) o;
            return id == that.id &&
                instrumentId == that.instrumentId &&
                modelId == that.modelId &&
                labId == that.labId &&
                labHead == that.labHead &&
                owner == that.owner &&
                invalid == that.invalid &&
                usedInExperiments == that.usedInExperiments &&
                sizeInBytes == that.sizeInBytes &&
                Objects.equals(name, that.name) &&
                Objects.equals(contentId, that.contentId) &&
                Objects.equals(uploadId, that.uploadId) &&
                Objects.equals(destinationPath, that.destinationPath) &&
                Objects.equals(instrumentName, that.instrumentName) &&
                Objects.equals(instrumentModel, that.instrumentModel) &&
                Objects.equals(labName, that.labName) &&
                Objects.equals(specieId, that.specieId) &&
                Objects.equals(vendorName, that.vendorName) &&
                accessLevel == that.accessLevel &&
                Objects.equals(labels, that.labels) &&
                Objects.equals(uploadDate, that.uploadDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                id,
                name,
                contentId,
                uploadId,
                destinationPath,
                instrumentId,
                instrumentName,
                modelId,
                instrumentModel,
                labName,
                labId,
                labHead,
                specieId,
                owner,
                invalid,
                vendorName,
                accessLevel,
                usedInExperiments,
                labels,
                sizeInBytes,
                uploadDate
            );
        }
    }

}
