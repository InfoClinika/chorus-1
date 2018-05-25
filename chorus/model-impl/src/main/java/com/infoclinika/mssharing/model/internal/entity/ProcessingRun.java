package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractExperiment;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

@Entity
@Table(name = "processing_runs")
public class ProcessingRun extends AbstractPersistable<Long>{

    @Column
    private String name;

    @Column(name = "processed_date")
    private Date processedDate = new Date();

    @ManyToMany(mappedBy = "processingRuns", cascade = {CascadeType.REFRESH, CascadeType.DETACH, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.EAGER)
    public Set<ProcessingFile> processingFiles = newHashSet();

    @ManyToOne(targetEntity = AbstractExperiment.class)
    @JoinColumn(name = "experiment_id")
    private AbstractExperiment experimentTemplate;

    public ProcessingRun() {
    }

    public ProcessingRun(String name, Date processedDate, Set<ProcessingFile> processingFiles, AbstractExperiment experimentTemplate) {
        this.name = name;
        this.processedDate = processedDate;
        this.processingFiles = processingFiles;
        this.experimentTemplate = experimentTemplate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getProcessedDate() {
        return processedDate;
    }

    public Set<ProcessingFile> getProcessingFiles() {
        return processingFiles;
    }

    public void setProcessingFiles(Set<ProcessingFile> processingFiles) {
        this.processingFiles = processingFiles;
    }

    public AbstractExperiment getExperimentTemplate() {
        return experimentTemplate;
    }

    public void setExperimentTemplate(AbstractExperiment experimentTemplate) {
        this.experimentTemplate = experimentTemplate;
    }


    public void addProcessingFile(ProcessingFile processingFile){
        if(processingFile != null){
            if(!processingFiles.contains(processingFile)){
                processingFiles.add(processingFile);
            }
        }
    }
}
