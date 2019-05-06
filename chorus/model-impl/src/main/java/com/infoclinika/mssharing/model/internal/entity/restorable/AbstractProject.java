package com.infoclinika.mssharing.model.internal.entity.restorable;

import com.infoclinika.mssharing.model.internal.entity.Group;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * @author Elena Kurilina
 */
@Entity(name = "Project")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AbstractProject extends ProjectTemplate<User, Lab, Group, AbstractProject> {

    private boolean blogEnabled;

    protected AbstractProject() {
    }

    public AbstractProject(User creator, Lab lab, String name, String areaOfResearch, String description) {
        setCreator(creator);
        setLab(lab);
        setName(name);
        setDescription(description);
        super.setAreaOfResearch(areaOfResearch);
    }

    public AbstractProject(long id) {
        setId(id);
    }

    protected void setId(long id) {
        super.setId(id);
    }

    public boolean isBlogEnabled() {
        return blogEnabled;
    }

    public void setBlogEnabled(boolean blogEnabled) {
        this.blogEnabled = blogEnabled;
    }
}
