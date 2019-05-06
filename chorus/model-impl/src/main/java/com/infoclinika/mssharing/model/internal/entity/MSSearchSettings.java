package com.infoclinika.mssharing.model.internal.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Vladislav Kovchug
 */
@Embeddable
public class MSSearchSettings {

    @Column(name = "mass_tolerance")
    private Double massTolerance;
    @Column(name = "rt_tolerance")
    private Double rtTolerance;
    @Column(name = "envelope_tolerance")
    private Double envelopeTolerance;
    @Column(name = "time_range_to_search")
    private Long timeRangeToSearch; // range to search(from now) in seconds.

    public MSSearchSettings(
        Double massTolerance,
        Double rtTolerance,
        Double envelopeTolerance,
        Long timeRangeToSearch
    ) {
        this.massTolerance = massTolerance;
        this.rtTolerance = rtTolerance;
        this.envelopeTolerance = envelopeTolerance;
        this.timeRangeToSearch = timeRangeToSearch;
    }

    public MSSearchSettings() {
    }

    public Double getMassTolerance() {
        return massTolerance;
    }

    public void setMassTolerance(Double massTolerance) {
        this.massTolerance = massTolerance;
    }

    public Double getRtTolerance() {
        return rtTolerance;
    }

    public void setRtTolerance(Double rtTolerance) {
        this.rtTolerance = rtTolerance;
    }

    public Double getEnvelopeTolerance() {
        return envelopeTolerance;
    }

    public void setEnvelopeTolerance(Double envelopeTolerance) {
        this.envelopeTolerance = envelopeTolerance;
    }

    public Long getTimeRangeToSearch() {
        return timeRangeToSearch;
    }

    public void setTimeRangeToSearch(Long timeRangeToSearch) {
        this.timeRangeToSearch = timeRangeToSearch;
    }
}
