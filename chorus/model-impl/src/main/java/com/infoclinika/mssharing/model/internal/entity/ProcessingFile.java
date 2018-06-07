package com.infoclinika.mssharing.model.internal.entity;


import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractExperiment;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "processed_file")
public class ProcessingFile extends AbstractPersistable<Long> {

    @Column(name = "name")
    private String name;

    @Column(name = "contentId", nullable = false)
    private String contentId;

    @Column(name = "upload_date")
    private Date uploadDate = new Date();

    @ManyToMany(targetEntity = ProcessingRun.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "processing_file_to_processing_runs", joinColumns = @JoinColumn(name = "processing_file_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "processing_run_id", referencedColumnName = "id"))
    private List<ProcessingRun> processingRuns = new ArrayList();

    @ManyToOne(targetEntity = AbstractExperiment.class, cascade = CascadeType.ALL)
    @JoinColumns({@JoinColumn(name = "experiment_id")})
    private AbstractExperiment experimentTemplate;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "processing_file_meta_data", joinColumns = @JoinColumn(name = "id_process_file", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "id_file_meta_data", referencedColumnName = "id"))
    private List<FileMetaDataTemplate> fileMetaDataTemplates = new ArrayList<>();

    @ManyToMany(targetEntity = ExperimentSample.class, cascade = {CascadeType.REFRESH,CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinTable(name = "sample_to_processed_file", joinColumns = @JoinColumn(name = "processed_file_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "sample_id", referencedColumnName = "id"))
    private List<ExperimentSample> experimentSamples = new ArrayList();



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

    public AbstractExperiment getExperiment() {
        return experimentTemplate;
    }

    public void setExperiment(AbstractExperiment experimentTemplate) {
        this.experimentTemplate = experimentTemplate;
    }


    public List<ExperimentSample> getExperimentSamples() {
        return experimentSamples;
    }

    public void setExperimentSamples(List<ExperimentSample> experimentSamples) {
        this.experimentSamples = experimentSamples;
    }

    public void addProcessingRun(ProcessingRun processingRun){
        if(processingRun != null){
            if(!processingRuns.contains(processingRun)){
                processingRuns.add(processingRun);
            }
        }
    }
}
