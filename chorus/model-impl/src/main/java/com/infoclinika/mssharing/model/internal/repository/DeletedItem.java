package com.infoclinika.mssharing.model.internal.repository;

import java.util.Date;

/**
 * @author Elena Kurilina
 */
public class DeletedItem {
    public final long id;
    public final Date deletionDate;
    public final String title;
    public final String type;
    public final String labName;

    public DeletedItem(long id, Date deletionDate, String title, String type, String labName) {
        this.id = id;
        this.deletionDate = deletionDate;
        this.title = title;
        this.type = type;
        this.labName = labName;
    }
}
