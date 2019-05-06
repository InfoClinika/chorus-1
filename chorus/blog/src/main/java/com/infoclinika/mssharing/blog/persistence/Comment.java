package com.infoclinika.mssharing.blog.persistence;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Pavel Kaplin
 */
@Entity
public class Comment {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private BlogPost post;

    private Author author = new Author();

    @Basic(optional = false)
    private Date date;

    @Lob
    private String content;

    public Long getId() {
        return id;
    }

    public BlogPost getPost() {
        return post;
    }

    public void setPost(BlogPost blogPost) {
        this.post = blogPost;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
