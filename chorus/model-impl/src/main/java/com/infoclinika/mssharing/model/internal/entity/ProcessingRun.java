package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractExperiment;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "processing_runs")
public class ProcessingRun extends AbstractPersistable<Long>{

    @Column
    private String name;

    @Column(name = "processed_date")
    private Date processedDate = new Date();

    @OneToMany(mappedBy = "processingRun", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public List<ProcessingFile> processingFiles = new ArrayList<>();


    @ManyToOne(targetEntity = AbstractExperiment.class)
    @JoinColumns({@JoinColumn(name = "experiment_id")})
    private AbstractExperiment experimentTemplate;

    public ProcessingRun() {
    }

    public ProcessingRun(String name, Date processedDate, List<ProcessingFile> processingFiles, AbstractExperiment experimentTemplate) {
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

    public void setProcessedDate(Date processedDate) {
        this.processedDate = processedDate;
    }

    public List<ProcessingFile> getProcessingFiles() {
        return processingFiles;
    }

    public void setProcessingFiles(List<ProcessingFile> processingFiles) {
        this.processingFiles = processingFiles;
    }

    public AbstractExperiment getExperimentTemplate() {
        return experimentTemplate;
    }

    public void setExperimentTemplate(AbstractExperiment experimentTemplate) {
        this.experimentTemplate = experimentTemplate;
    }
}
