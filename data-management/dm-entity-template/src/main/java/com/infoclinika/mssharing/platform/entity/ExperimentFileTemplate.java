package com.infoclinika.mssharing.platform.entity;

import com.google.common.collect.Lists;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Herman Zamula
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ExperimentFileTemplate<F extends FileMetaDataTemplate<?, ?>, E extends ExperimentTemplate<?, ?, ?, ?, ?, ?>, ANNOTATION extends AnnotationTemplate
        > extends AbstractPersistable {

    @ManyToOne(fetch = FetchType.EAGER)
    private F fileMetaData;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "e_file_id")
    private List<ANNOTATION> annotationList = Lists.newArrayList();

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> factorValues = Lists.newArrayList();
    @ManyToMany(mappedBy = "files", cascade = {CascadeType.REFRESH, CascadeType.DETACH, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private Set<Condition> conditions = newHashSet();
    @Column
    private boolean isCopy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private E experiment;

    public ExperimentFileTemplate(F fileMetaData, List<String> factorValues) {
        setFileMetaData(fileMetaData);
        getFactorValues().addAll(factorValues);
    }

    public ExperimentFileTemplate() {
    }

    public F getFileMetaData() {
        return fileMetaData;
    }

    public void setFileMetaData(F origin) {
        this.fileMetaData = origin;
    }

    public List<String> getFactorValues() {
        return factorValues;
    }

    public Set<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(Set<Condition> conditions) {
        this.conditions = conditions;
    }

    public boolean isCopy() {
        return isCopy;
    }

    public void setCopy(boolean copy) {
        isCopy = copy;
    }

    public E getExperiment() {
        return experiment;
    }

    public void setExperiment(E experiment) {
        this.experiment = experiment;
    }

    public List<ANNOTATION> getAnnotationList() {
        return annotationList;
    }

}
