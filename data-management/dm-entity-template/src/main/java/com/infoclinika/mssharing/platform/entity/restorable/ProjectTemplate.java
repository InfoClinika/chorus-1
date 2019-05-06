package com.infoclinika.mssharing.platform.entity.restorable;

import com.infoclinika.mssharing.platform.entity.*;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Herman Zamula
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class ProjectTemplate<U extends UserTemplate<?>,
    L extends LabTemplate<?>,
    G extends GroupTemplate<U>,
    P extends ProjectTemplate<U, L, G, P>> extends AbstractRestorable {

    @OneToMany(cascade = {CascadeType.REFRESH, CascadeType.DETACH, CascadeType.PERSIST, CascadeType.MERGE})
    protected List<Attachment<U>> attachments = newArrayList();
    private String areaOfResearch;

    @ManyToOne
    private U creator;

    @ManyToOne
    private L lab;

    @Index(name = "PROJECT_NAME_IDX")
    private String name;

    @Lob
    private String description;
    private Sharing<U, P, G> sharing = new Sharing<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public U getCreator() {
        return creator;
    }

    public void setCreator(U creator) {
        this.creator = creator;
    }

    public L getLab() {
        return lab;
    }

    public void setLab(L lab) {
        this.lab = lab;
    }

    public Sharing<U, P, G> getSharing() {
        if (sharing == null) {
            sharing = new Sharing<>();
        }

        return sharing;
    }

    public void setSharing(Sharing<U, P, G> sharing) {
        this.sharing = sharing;
    }

    public String getAreaOfResearch() {
        return areaOfResearch;
    }

    public void setAreaOfResearch(String areaOfResearch) {
        this.areaOfResearch = areaOfResearch;
    }

    public List<Attachment<U>> getAttachments() {
        return attachments;
    }
}
