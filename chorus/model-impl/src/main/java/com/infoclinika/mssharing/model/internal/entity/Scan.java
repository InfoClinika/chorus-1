package com.infoclinika.mssharing.model.internal.entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author andrii.loboda
 */
@Entity
public class Scan extends AbstractPersistable<Long> {
    @Column(name = "rt")
    private float rt;

    @Column(name = "tic")
    private float tic;

    @Column(name = "bpi_mass")
    private float bpiMass;

    @Column(name = "bpi")
    private float bpi;

    @Column(name = "filter")
    private int filter;

    @Column(name = "number")
    private int number;

    @Column(name = "parent_mass")
    private float parentMass;

    @ManyToOne
    private RawFile rawFile;

    @ManyToMany
    @JoinTable(name = "raw_file_scan",
        joinColumns = @JoinColumn(name = "scans_id", referencedColumnName = "id", nullable = false),
        inverseJoinColumns = @JoinColumn(name = "raw_file_id", referencedColumnName = "id", nullable = false)
    )
    private List<RawFile> rawFiles = newArrayList();

    Scan() {
    }

    Scan(
        float rt,
        float tic,
        float bpiMass,
        float bpi,
        int filter,
        int number,
        float parentMass,
        RawFile rawFile,
        List<RawFile> rawFiles
    ) {
        this.rt = rt;
        this.tic = tic;
        this.bpi = bpi;
        this.bpiMass = bpiMass;
        this.filter = filter;
        this.number = number;
        this.parentMass = parentMass;
        this.rawFile = rawFile;
        this.rawFiles = rawFiles;
    }

    public float getRt() {
        return rt;
    }

    public float getTic() {
        return tic;
    }

    public float getBpiMass() {
        return bpiMass;
    }

    public float getBpi() {
        return bpi;
    }

    public int getFilter() {
        return filter;
    }

    public int getNumber() {
        return number;
    }

    public float getParentMass() {
        return parentMass;
    }

    public RawFile getRawFile() {
        return rawFile;
    }

    public List<RawFile> getRawFiles() {
        return rawFiles;
    }
}

