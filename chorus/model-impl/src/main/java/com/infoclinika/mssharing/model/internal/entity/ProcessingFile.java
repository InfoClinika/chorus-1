package com.infoclinika.mssharing.model.internal.entity;


import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractExperiment;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.security.access.method.P;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

@Entity
@Table(name = "processed_file")
public class ProcessingFile extends AbstractPersistable<Long> {

    @Column(name = "name")
    private String name;

    @Column(name = "contentId", nullable = false)
    private String contentId;

    @Column(name = "upload_date")
    private Date uploadDate = new Date();

    @ManyToMany(targetEntity = ProcessingRun.class)
    @JoinTable(name = "processing_file_to_processing_runs", joinColumns = @JoinColumn(name = "processing_file_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "processing_run_id", referencedColumnName = "id"))
    private List<ProcessingRun> processingRuns = new ArrayList();

    @ManyToOne(cascade = CascadeType.ALL)
    private AbstractExperiment experimentTemplate;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "processing_file_meta_data", joinColumns = @JoinColumn(name = "id_process_file", referencedColumnName = "id", nullable = false, updatable = false),
    inverseJoinColumns = @JoinColumn(name = "id_file_meta_data", referencedColumnName = "id", nullable = false, updatable = false))
    private List<FileMetaDataTemplate> fileMetaDataTemplates = new ArrayList<>();

    public ProcessingFile(String name, String contentId, Date uploadDate, List<ProcessingRun> processingRuns, AbstractExperiment experimentTemplate, List<FileMetaDataTemplate> fileMetaDataTemplates) {
        this.name = name;
        this.contentId = contentId;
        this.uploadDate = uploadDate;
        this.processingRuns = processingRuns;
        this.experimentTemplate = experimentTemplate;
        this.fileMetaDataTemplates = fileMetaDataTemplates;
    }

    public ProcessingFile(String name, String contentId, AbstractExperiment experimentTemplate) {
        this.name = name;
        this.contentId = contentId;
        this.experimentTemplate = experimentTemplate;
    }

    public ProcessingFile() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uplpoadDate) {
        this.uploadDate = uplpoadDate;
    }

    public List<FileMetaDataTemplate> getFileMetaDataTemplates() {
        return fileMetaDataTemplates;
    }

    public List<ProcessingRun> getProcessingRuns() {
        return processingRuns;
    }

    public void setProcessingRuns(List<ProcessingRun> processingRuns) {
        this.processingRuns = processingRuns;
    }

    public AbstractExperiment getExperimentTemplate() {
        return experimentTemplate;
    }

    public void setExperimentTemplate(AbstractExperiment experimentTemplate) {
        this.experimentTemplate = experimentTemplate;
    }

    public void addProcessingRun(ProcessingRun processingRun){
        if(processingRun != null){
            if(!processingRuns.contains(processingRun)){
                processingRuns.add(processingRun);
            }
        }
    }
}
