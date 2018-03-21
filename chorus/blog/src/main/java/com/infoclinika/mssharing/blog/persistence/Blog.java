package com.infoclinika.mssharing.blog.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Formula;

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Pavel Kaplin
 */
@Entity
@JsonIgnoreProperties("subscribers")
public class Blog {
    @Id
    private Long id;

    @Basic(optional = false)
    private String name;

    @ElementCollection
    private Set<Long> subscribers = new HashSet<Long>();

    @Formula("(select count(*) from Blog_subscribers s where s.Blog_id = id)")
    private int subscribersCount;

    private boolean enabled = true;

    public Blog() {
    }

    public Blog(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Long> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Set<Long> subscribers) {
        this.subscribers = subscribers;
    }

    public int getSubscribersCount() {
        return subscribersCount;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
