package com.infoclinika.mssharing.blog.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Pavel Kaplin
 */
@Entity
@JsonIgnoreProperties("subscribers")
public class BlogPost {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Blog blog;

    private Author author = new Author();

    @Basic(optional = false)
    private Date date;

    private Date lastEdited;

    @Basic(optional = false)
    @Lob
    private String title;

    @Lob
    private String content;

    @Formula("(select count(*) from Comment c where c.post_id = id)")
    private int commentsCount;

    @Formula("(select count(*) from BlogPost_subscribers s where s.BlogPost_id = id)")
    private int subscribersCount;

    @ElementCollection
    private Set<Long> subscribers = new HashSet<Long>();

    public Long getId() {
        return id;
    }

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getLastEdited() {
        return lastEdited;
    }

    public void setLastEdited(Date lastEdited) {
        this.lastEdited = lastEdited;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCommentsCount() {
        return commentsCount;
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
}
