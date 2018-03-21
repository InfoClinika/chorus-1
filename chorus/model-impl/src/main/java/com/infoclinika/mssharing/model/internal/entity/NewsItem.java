package com.infoclinika.mssharing.model.internal.entity;

import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.Date;

@Entity
public class NewsItem extends AbstractAggregate{

    private String title;
    @Lob
    private String text;
    @Lob
    private String introduction;
    private String author;
    private Date creationDate;

    public NewsItem(String title,String introduction, String text, String author, Date creationDate) {
        this.title = title;
        this.introduction = introduction;
        this.text = text;
        this.author = author;
        this.creationDate = creationDate;
    }

    public NewsItem(){}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
}
