package com.infoclinika.mssharing.model.internal.stats;

import java.util.Date;

public class DateRange {
    private final Date from;
    private final Date to;

    public DateRange(Date from, Date to) {
        this.from = from;
        this.to = to;
    }

    public Date getFrom() {
        return from;
    }

    public Date getTo() {
        return to;
    }


    @Override
    public String toString() {
        return "DateRange{" +
            "from=" + from +
            ", to=" + to +
            '}';
    }
}
