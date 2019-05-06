package com.infoclinika.mssharing.blog.persistence;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Pavel Kaplin
 */
@Embeddable
public class Author {
    @Column(name = "author_id")
    private long id;

    @Column(name = "author_name")
    @Basic(optional = false)
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
