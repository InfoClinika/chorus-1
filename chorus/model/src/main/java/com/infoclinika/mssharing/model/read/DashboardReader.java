/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.read;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.read.*;
import com.infoclinika.mssharing.platform.model.read.InstrumentModelReaderTemplate.InstrumentModelLineTemplate;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate.LabLineTemplate;

import java.util.*;

import static com.infoclinika.mssharing.platform.model.read.GroupsReaderTemplate.GroupLine;

/**
 * Provides data that could be read at main section of dashboard.
 *
 * @author Stanislav Kurilin
 */
public interface DashboardReader extends
        ProjectReaderTemplate<ProjectLine>,
        ExperimentReaderTemplate<ExperimentLine>,
        InstrumentReaderTemplate<InstrumentLine>,
        LabReaderTemplate<LabLineTemplate>,
        FileReaderTemplate<FileLine>,
        GroupsReaderTemplate<GroupLine>,
        InstrumentModelReaderTemplate<InstrumentModelLineTemplate>{

    //TODO: Used only in tests
    ImmutableSet<InstrumentItem> instrumentsWithAvailableFiles(long actor);

    InstrumentItem readInstrument(long instrument);

    ImmutableSet<UserLine> readUsersByLab(long labHead, long lab);

    String getChartsUrlForFiles(long user, List<Long> files);

    FullFolderStructure readFolderStructure(long user);

    FolderStructure readFolderStructure(long user, Filter filter);

    /**
     * Return features which are enabled or disabled in application for user
     */
    Map<String, Boolean> getFeatures(long actor);

    /**
     * @param actor user for whom features are requested.
     * @return map in which key is a name of feature, value is a feature itself.
     */
    Map<String, FeatureItem> getFeatureItems(long actor);

    Set<ExperimentLevelItem> readExperimentLevels(long userId, long experiment);

    ProjectSharingRequestInfo readProjectSharingRequest(long user, long accessExperimentId);

    SortedSet<ProjectStructure> readProjectsOnlyStructure(long userId, Filter filter);

    SortedSet<ExperimentStructure> readExperimentsOnlyStructureByProject(long userId, long projectId);

    SortedSet<UploadedFile> readFilesStructureByExperiment(long userId, long id);

    SortedSet<ExperimentStructure> readExperimentsOnlyStructure(long userId, Filter filter);

    SortedSet<UploadedFile> readFilesOnlyStructure(long userId, Filter filter);

    class ExperimentLevelItem {
        public final long id;
        public final String value;
        public final ExperimentFactorItem factor;
        public final Set<Long> conditions;

        public ExperimentLevelItem(long id, String value, ExperimentFactorItem factor, Set<Long> conditions) {
            this.id = id;
            this.value = value;
            this.factor = factor;
            this.conditions = conditions;
        }

        public static class ExperimentFactorItem {
            public final long id;
            public final String factorName;

            public ExperimentFactorItem(long id, String factorName) {
                this.id = id;
                this.factorName = factorName;
            }
        }

    }

    class FileColumns {
        public final String name;
        public final long sizeInBytes;
        public final String instrument;
        public final String laboratory;
        public final Date uploadDate;
        public final String labels;

        //From Annotations
        public Date creationDate;
        public String comment;
        public String instrumentMethod;
        public String startRt;
        public String endRt;
        public String startMz;
        public String endMz;
        public String fileName;
        public String seqRowPosition;
        public String sampleName;
        public String annotationInstrument;
        public String userName;
        public String userLabels;
        public String fileCondition;
        public String instrumentSerialNumber;
        public String phone;
        public String instrumentName;

        public FileColumns(String name, long sizeInBytes, String instrument, String laboratory, Date uploadDate, String labels) {
            this.name = name;
            this.sizeInBytes = sizeInBytes;
            this.instrument = instrument;
            this.laboratory = laboratory;
            this.uploadDate = uploadDate;
            this.labels = labels;
        }


    }

    class ProjectColumns {
        public final String name;
        public final String owner;
        public final String laboratory;
        public final String area;
        public final Date modified;

        public ProjectColumns(String name, String owner, String laboratory, String area, Date modified) {
            this.name = name;
            this.owner = owner;
            this.laboratory = laboratory;
            this.area = area;
            this.modified = modified;
        }
    }

    class ExperimentColumns {
        public final String name;
        public final String owner;
        public final String laboratory;
        public final String project;
        public final long files;
        public final Date modified;

        public ExperimentColumns(String name, String owner, String laboratory, String project, long files, Date modified) {
            this.name = name;
            this.owner = owner;
            this.laboratory = laboratory;
            this.project = project;
            this.files = files;
            this.modified = modified;
        }
    }

    enum StorageStatus {
        ARCHIVED, UNARCHIVED, ARCHIVING_IN_PROCESS, UN_ARCHIVING_IN_PROCESS, UN_ARCHIVING_FOR_DOWNLOAD_IN_PROCESS
    }

    class UserLine {
        public final long id;
        public final String email;
        public final String firstName;
        public final String lastName;
        public final boolean labHead;

        public UserLine(long id, String email, String firstName, String lastName, boolean labHead) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.labHead = labHead;
        }
    }

    class FullFolderStructure {
        public final SortedSet<ProjectStructure> myProjects = new TreeSet<>();
        public final SortedSet<ProjectStructure> sharedProjects = new TreeSet<>();
        public final SortedSet<ProjectStructure> publicProjects = new TreeSet<>();

        public final SortedSet<ExperimentStructure> myExperiments = new TreeSet<>();
        public final SortedSet<ExperimentStructure> sharedExperiments = new TreeSet<>();
        public final SortedSet<ExperimentStructure> publicExperiments = new TreeSet<>();

        public final SortedSet<UploadedFile> myFiles = new TreeSet<>();
        public final SortedSet<UploadedFile> sharedFiles = new TreeSet<>();
        public final SortedSet<UploadedFile> publicFiles = new TreeSet<>();
    }

    class FolderStructure {
        public final SortedSet<ProjectStructure> projects = new TreeSet<>();
        public final SortedSet<ExperimentStructure> experiments = new TreeSet<>();
        public final SortedSet<UploadedFile> files = new TreeSet<>();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FolderStructure that = (FolderStructure) o;

            if (experiments != null ? !experiments.equals(that.experiments) : that.experiments != null) return false;
            if (files != null ? !files.equals(that.files) : that.files != null) return false;
            if (projects != null ? !projects.equals(that.projects) : that.projects != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = projects != null ? projects.hashCode() : 0;
            result = 31 * result + (experiments != null ? experiments.hashCode() : 0);
            result = 31 * result + (files != null ? files.hashCode() : 0);
            return result;
        }
    }

    class ProjectStructure implements Comparable<ProjectStructure> {
        public final long id;
        public final String projectName;
        public final String labName;
        public final String owner;
        public final Date lastModified;
        public final SortedSet<ExperimentStructure> experiments = new TreeSet<>();

        public ProjectStructure(long id, String projectName, String labName, String owner, Date lastModified) {
            this.id = id;
            this.projectName = projectName;
            this.labName = labName;
            this.owner = owner;
            this.lastModified = lastModified;
        }

        @Override
        public int compareTo(ProjectStructure o) {
            final int nameComparison = this.projectName.compareTo(o.projectName);
            return nameComparison == 0 ? Long.compare(this.id, o.id) : nameComparison;

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ProjectStructure that = (ProjectStructure) o;

            if (id != that.id) return false;
            if (experiments != null ? !experiments.equals(that.experiments) : that.experiments != null) return false;
            if (labName != null ? !labName.equals(that.labName) : that.labName != null) return false;
            if (lastModified != null ? !lastModified.equals(that.lastModified) : that.lastModified != null)
                return false;
            if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
            if (projectName != null ? !projectName.equals(that.projectName) : that.projectName != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (id ^ (id >>> 32));
            result = 31 * result + (projectName != null ? projectName.hashCode() : 0);
            result = 31 * result + (labName != null ? labName.hashCode() : 0);
            result = 31 * result + (owner != null ? owner.hashCode() : 0);
            result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
            result = 31 * result + (experiments != null ? experiments.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ProjectStructure{" +
                    "id=" + id +
                    ", projectName='" + projectName + '\'' +
                    ", labName='" + labName + '\'' +
                    ", owner='" + owner + '\'' +
                    ", lastModified=" + lastModified +
                    ", experiments=" + experiments +
                    '}';
        }
    }

    class ExperimentStructure implements Comparable<ExperimentStructure> {
        public final long id;
        public final String experimentName;
        public final String owner;
        public final boolean currentUserOwner;
        public final Date lastModified;
        public final long analysisRunCount;
        public final SortedSet<UploadedFile> files = new TreeSet<>();

        public ExperimentStructure(long id, String experimentName, String owner, boolean currentUserOwner, Date lastModified, int analysisRunCount) {
            this.id = id;
            this.experimentName = experimentName;
            this.owner = owner;
            this.currentUserOwner = currentUserOwner;
            this.lastModified = lastModified;
            this.analysisRunCount = analysisRunCount;
        }

        @Override
        public int compareTo(ExperimentStructure o) {
            final int nameComparison = this.experimentName.compareTo(o.experimentName);
            return nameComparison == 0 ? Long.compare(this.id, o.id) : nameComparison;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ExperimentStructure that = (ExperimentStructure) o;

            if (analysisRunCount != that.analysisRunCount) return false;
            if (currentUserOwner != that.currentUserOwner) return false;
            if (id != that.id) return false;
            if (experimentName != null ? !experimentName.equals(that.experimentName) : that.experimentName != null)
                return false;
            if (files != null ? !files.equals(that.files) : that.files != null) return false;
            if (lastModified != null ? !lastModified.equals(that.lastModified) : that.lastModified != null)
                return false;
            if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (id ^ (id >>> 32));
            result = 31 * result + (experimentName != null ? experimentName.hashCode() : 0);
            result = 31 * result + (owner != null ? owner.hashCode() : 0);
            result = 31 * result + (currentUserOwner ? 1 : 0);
            result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
            result = 31 * result + (int) (analysisRunCount ^ (analysisRunCount >>> 32));
            result = 31 * result + (files != null ? files.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ExperimentStructure{" +
                    "id=" + id +
                    ", experimentName='" + experimentName + '\'' +
                    ", owner='" + owner + '\'' +
                    ", currentUserOwner=" + currentUserOwner +
                    ", lastModified=" + lastModified +
                    ", analysisRunCount=" + analysisRunCount +
                    ", files=" + files +
                    '}';
        }
    }

    class UploadedFile implements Comparable<UploadedFile> {
        public final long id;
        public final String name;
        public final String instrumentName;
        public final String instrumentModel;
        public final Date uploadDate;
        public final Date acquisitionDate;
        public final long fileSizeBytes;
        public final long ownerId;

        public UploadedFile(long id, String name, String instrumentName, String instrumentModel,
                            Date uploadDate, Date acquisitionDate, long fileSizeBytes, long ownerId) {
            this.id = id;
            this.name = name;
            this.instrumentName = instrumentName;
            this.instrumentModel = instrumentModel;
            this.uploadDate = uploadDate;
            this.acquisitionDate = acquisitionDate;
            this.fileSizeBytes = fileSizeBytes;
            this.ownerId = ownerId;
        }

        @Override
        public int compareTo(UploadedFile o) {
            final int nameComparison = this.name.compareTo(o.name);
            return nameComparison == 0 ? Long.compare(this.id, o.id) : nameComparison;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UploadedFile that = (UploadedFile) o;

            if (fileSizeBytes != that.fileSizeBytes) return false;
            if (id != that.id) return false;
            if (ownerId != that.ownerId) return false;
            if (acquisitionDate != null ? !acquisitionDate.equals(that.acquisitionDate) : that.acquisitionDate != null)
                return false;
            if (instrumentModel != null ? !instrumentModel.equals(that.instrumentModel) : that.instrumentModel != null)
                return false;
            if (instrumentName != null ? !instrumentName.equals(that.instrumentName) : that.instrumentName != null)
                return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (uploadDate != null ? !uploadDate.equals(that.uploadDate) : that.uploadDate != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (id ^ (id >>> 32));
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (instrumentName != null ? instrumentName.hashCode() : 0);
            result = 31 * result + (instrumentModel != null ? instrumentModel.hashCode() : 0);
            result = 31 * result + (uploadDate != null ? uploadDate.hashCode() : 0);
            result = 31 * result + (acquisitionDate != null ? acquisitionDate.hashCode() : 0);
            result = 31 * result + (int) (fileSizeBytes ^ (fileSizeBytes >>> 32));
            result = 31 * result + (int) (ownerId ^ (ownerId >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "UploadedFile{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", instrumentName='" + instrumentName + '\'' +
                    ", instrumentModel='" + instrumentModel + '\'' +
                    ", uploadDate=" + uploadDate +
                    ", acquisitionDate=" + acquisitionDate +
                    ", fileSizeBytes=" + fileSizeBytes +
                    ", ownerId=" + ownerId +
                    '}';
        }
    }


    class ProjectSharingRequestInfo {
        public final List<String> downloadExperimentLinks;

        public ProjectSharingRequestInfo(List<String> downloadExperimentLinks) {
            this.downloadExperimentLinks = downloadExperimentLinks;
        }
    }

    class FeatureItem {
        public final boolean enabledGlobally;
        public final Set<Long> enabledForLabs;

        public FeatureItem(boolean enabledGlobally, Set<Long> enabledForLabs) {
            this.enabledGlobally = enabledGlobally;
            this.enabledForLabs = enabledForLabs;
        }
    }

}
