package com.infoclinika.mssharing.platform.entity.restorable;

import com.infoclinika.mssharing.platform.entity.AbstractAggregate;

import javax.persistence.MappedSuperclass;

/**
 * Marker for restorable domain entities
 *
 * @author Herman Zamula
 */
@MappedSuperclass
public abstract class AbstractRestorable extends AbstractAggregate {

    private boolean isDeleted;

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
