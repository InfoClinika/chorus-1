package com.infoclinika.mssharing.services.billing.rest.api.model;

import java.util.Date;

/**
 * @author Herman Zamula
 */
public class HistoryForMonthReference {

    public Date monthYear;
    public String csvDataReference;
    public boolean hasNext;

    public HistoryForMonthReference() {

    }

    public HistoryForMonthReference(Date monthYear, String csvDataReference, boolean hasNext) {
        this.monthYear = monthYear;
        this.csvDataReference = csvDataReference;
        this.hasNext = hasNext;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoryForMonthReference that = (HistoryForMonthReference) o;

        return monthYear.equals(that.monthYear);

    }

    @Override
    public int hashCode() {
        return monthYear.hashCode();
    }
}
