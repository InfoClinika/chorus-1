package com.infoclinika.mssharing.model.internal.entity.restorable;

import com.infoclinika.mssharing.model.internal.FileCompound;
import com.infoclinika.mssharing.model.internal.entity.AbstractAggregate;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

/**
 * @author : Alexander Serebriyan
 */
@Entity
@Table(name = "external_file_metadata")
public class ExternalFileMetadata extends AbstractAggregate {

    @Column(name = "related_refcon")
    private String relatedRefcon;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "external_metadata_id")
    private Set<FileCompound> fileCompounds;

    @Column(name = "processing_type")
    @Enumerated(value = EnumType.STRING)
    private ProcessingType processingType;

    @Column(name = "polarity")
    @Enumerated(value = EnumType.STRING)
    private Polarity polarity;

    @Enumerated(value = EnumType.STRING)
    private FileType type;

    @Column(name = "acq_index")
    private int acqIndex;

    public ExternalFileMetadata() {
    }

    public ExternalFileMetadata(String relatedRefcon,
                                Set<FileCompound> fileCompounds,
                                FileType type,
                                ProcessingType processingType,
                                Polarity polarity,
                                int acqIndex
    ) {
        this.relatedRefcon = relatedRefcon;
        this.fileCompounds = fileCompounds;
        this.type = type;
        this.processingType = processingType;
        this.polarity = polarity;
        this.acqIndex = acqIndex;
    }

    public String getRelatedRefcon() {
        return relatedRefcon;
    }

    public void setRelatedRefcon(String relatedRefcon) {
        this.relatedRefcon = relatedRefcon;
    }

    public Set<FileCompound> getFileCompounds() {
        return fileCompounds;
    }

    public void setFileCompounds(Set<FileCompound> fileCompounds) {
        this.fileCompounds = fileCompounds;
    }

    public FileType getFileType() {
        return type;
    }

    public void setFileType(FileType fileType) {
        this.type = fileType;
    }

    public ProcessingType getProcessingType() {
        return processingType;
    }

    public void setProcessingType(ProcessingType processingType) {
        this.processingType = processingType;
    }

    public Polarity getPolarity() {
        return polarity;
    }

    public void setPolarity(Polarity polarity) {
        this.polarity = polarity;
    }

    public int getAcqIndex() {
        return acqIndex;
    }

    public void setAcqIndex(int acqIndex) {
        this.acqIndex = acqIndex;
    }

    @Override
    public String toString() {
        return "ExternalFileMetadata{" +
            "relatedRefcon='" + relatedRefcon + '\'' +
            ", fileCompounds=" + fileCompounds +
            ", processingType=" + processingType +
            ", type=" + type +
            ", acqIndex=" + acqIndex +
            '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExternalFileMetadata)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ExternalFileMetadata that = (ExternalFileMetadata) o;
        return acqIndex == that.acqIndex &&
            Objects.equals(relatedRefcon, that.relatedRefcon) &&
            Objects.equals(fileCompounds, that.fileCompounds) &&
            processingType == that.processingType &&
            polarity == that.polarity &&
            type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            super.hashCode(),
            relatedRefcon,
            fileCompounds,
            processingType,
            polarity,
            type,
            acqIndex
        );
    }

    public enum FileType {
        SAMPLE,
        REFCON,
        SYSCON
    }

    public enum ProcessingType {
        COMPOUNDS,
        PEPTIDES,
        NATURAL_PRODUCTS
    }

    public enum Polarity {
        POSITIVE,
        NEGATIVE,
        BOTH,
        UNKNOWN
    }
}
