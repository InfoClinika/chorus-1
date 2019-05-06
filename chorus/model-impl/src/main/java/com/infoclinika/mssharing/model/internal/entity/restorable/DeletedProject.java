package com.infoclinika.mssharing.model.internal.entity.restorable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import java.util.Date;

/**
 * @author Elena Kurilina
 */
@Entity
public class DeletedProject extends AbstractProject {
    @Basic
    public Date deletionDate = new Date();

    protected DeletedProject() {
    }

    public DeletedProject(AbstractProject p) {
        super(p.getCreator(), p.getLab(), p.getName(), p.getAreaOfResearch(), p.getDescription());
        this.deletionDate = new Date();
        this.setBlogEnabled(p.isBlogEnabled());
        this.setSharing(p.getSharing());
        this.attachments = p.getAttachments();
        this.setDeleted(true);
    }

}
