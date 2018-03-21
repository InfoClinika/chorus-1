package com.infoclinika.mssharing.model.read;

import com.infoclinika.mssharing.platform.model.read.AccessLevel;
import com.infoclinika.mssharing.platform.model.read.ExperimentReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;

import java.util.Date;

/**
 * @author Herman Zamula
 */
public class ExperimentLine extends ExperimentReaderTemplate.ExperimentLineTemplate {

    public final boolean isOwner;
    public final String msChartsUrl;
//    public final String translationErrors;
//    public final Date lastTranslationAttemptDate;
    public final boolean isAvailableForCopying;
    public final boolean downloadAvailable;
    public final boolean hasUnArchiveRequest;
    public final boolean hasUnArchiveDownloadOnlyRequest;
//    public final boolean translationAvailable;
    public final boolean canUnarchive;
    public final boolean canArchive;
    public final int analyzesCount;
    public final Long billLab;
//    public final DashboardReader.TranslationStatus translationStatus;
    public final DashboardReader.ExperimentColumns columns;

    public ExperimentLine(long id, LabReaderTemplate.LabLineTemplate lab, String name, String creator, String project,
                          long files, Date modified, AccessLevel accessLevel, String msChartsUrl,
                          String downloadLink,
                          boolean owner, boolean isAvailableForCopying, boolean downloadAvailable, boolean hasUnArchiveRequest, boolean hasUnArchiveDownloadOnlyRequest,
                          boolean canArchive, boolean canUnarchive,
                          int analyzesCount,
                          Long billLab, long ownerId,
                          DashboardReader.ExperimentColumns columns) {
        super(id, name, project, files, modified, lab, downloadLink, creator, accessLevel, ownerId);
        this.msChartsUrl = msChartsUrl;
        isOwner = owner;
        this.isAvailableForCopying = isAvailableForCopying;
        this.downloadAvailable = downloadAvailable;
        this.hasUnArchiveRequest = hasUnArchiveRequest;
        this.hasUnArchiveDownloadOnlyRequest = hasUnArchiveDownloadOnlyRequest;
        this.canArchive = canArchive;
        this.canUnarchive = canUnarchive;
        this.analyzesCount = analyzesCount;
        this.billLab = billLab;
        this.columns = columns;
    }

    public ExperimentLine(ExperimentReaderTemplate.ExperimentLineTemplate other,
                          boolean isOwner, String msChartsUrl,
                          boolean isAvailableForCopying, boolean downloadAvailable, boolean hasUnArchiveRequest, boolean hasUnArchiveDownloadOnlyRequest,
                          boolean canUnarchive, boolean canArchive,
                          int analyzesCount, Long billLab,
                          DashboardReader.ExperimentColumns columns) {
        super(other);
        this.isOwner = isOwner;
        this.msChartsUrl = msChartsUrl;
        this.isAvailableForCopying = isAvailableForCopying;
        this.downloadAvailable = downloadAvailable;
        this.hasUnArchiveRequest = hasUnArchiveRequest;
        this.hasUnArchiveDownloadOnlyRequest = hasUnArchiveDownloadOnlyRequest;
        this.canUnarchive = canUnarchive;
        this.canArchive = canArchive;
        this.analyzesCount = analyzesCount;
        this.billLab = billLab;
        this.columns = columns;
    }

    public ExperimentLine(ExperimentReaderTemplate.ExperimentLineTemplate lineTemplate, long billLab) {
        this(lineTemplate, false, null, false, false, false, false, false, false,
                0, billLab, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExperimentLine)) return false;
        if (!super.equals(o)) return false;

        ExperimentLine that = (ExperimentLine) o;

        if (analyzesCount != that.analyzesCount) return false;
        if (canArchive != that.canArchive) return false;
        if (canUnarchive != that.canUnarchive) return false;
        if (downloadAvailable != that.downloadAvailable) return false;
        if (hasUnArchiveDownloadOnlyRequest != that.hasUnArchiveDownloadOnlyRequest) return false;
        if (hasUnArchiveRequest != that.hasUnArchiveRequest) return false;
        if (isAvailableForCopying != that.isAvailableForCopying) return false;
        if (isOwner != that.isOwner) return false;
        if (billLab != null ? !billLab.equals(that.billLab) : that.billLab != null) return false;
        if (columns != null ? !columns.equals(that.columns) : that.columns != null) return false;
        if (msChartsUrl != null ? !msChartsUrl.equals(that.msChartsUrl) : that.msChartsUrl != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isOwner ? 1 : 0);
        result = 31 * result + (msChartsUrl != null ? msChartsUrl.hashCode() : 0);
        result = 31 * result + (isAvailableForCopying ? 1 : 0);
        result = 31 * result + (downloadAvailable ? 1 : 0);
        result = 31 * result + (hasUnArchiveRequest ? 1 : 0);
        result = 31 * result + (hasUnArchiveDownloadOnlyRequest ? 1 : 0);
        result = 31 * result + (canUnarchive ? 1 : 0);
        result = 31 * result + (canArchive ? 1 : 0);
        result = 31 * result + analyzesCount;
        result = 31 * result + (billLab != null ? billLab.hashCode() : 0);
        result = 31 * result + (columns != null ? columns.hashCode() : 0);
        return result;
    }
}
