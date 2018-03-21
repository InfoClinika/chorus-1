package com.infoclinika.mssharing.platform.entity.restorable;

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.persistence.Lob;

/**
 * @author Stanislav Kurilin
 */
@Embeddable
public class ExperimentData {

    @Lob
    private String description;

    @Basic(optional = false)
    private boolean is2dLc;

    public ExperimentData() {
    }

    public ExperimentData(String description, boolean is2dLc) {
        this.description = description;
        this.is2dLc = is2dLc;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean is2dLc() {
        return is2dLc;
    }
}
