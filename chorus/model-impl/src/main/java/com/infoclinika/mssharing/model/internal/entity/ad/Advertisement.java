package com.infoclinika.mssharing.model.internal.entity.ad;

import com.infoclinika.mssharing.model.internal.entity.AbstractAggregate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Represents ads displayed for Chorus's users.
 *
 * @author andrii.loboda
 */
@Entity
@Table(name = "a_Advertisement")
public class Advertisement extends AbstractAggregate {
    @Basic(optional = false)
    private String title;
    @Basic(optional = false)
    @Column(length = 1024)
    private String redirectLink;

    private String imageToDisplayRef; // Cloud reference path in string

    private String imageName;
    private long imageSize;
    @Basic(optional = false)
    private Date startRollDate;
    @Basic(optional = false)
    private Date endRollDate;

    @Basic(optional = false)
    private long clickedCount;
    @Basic(optional = false)
    private long displayedCount;
    @Basic(optional = false)
    private boolean isEnabled;

    public Advertisement(String title, String redirectLink, Date startRollDate, Date endRollDate, String imageName, long imageSize, boolean isEnabled) {
        this.title = title;
        this.redirectLink = redirectLink;
        this.startRollDate = startRollDate;
        this.endRollDate = endRollDate;
        this.imageName = imageName;
        this.imageSize = imageSize;
        this.isEnabled = isEnabled;
    }

    /*automatically generated getters, setters, default package constructor*/
    Advertisement() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRedirectLink() {
        return redirectLink;
    }

    public void setRedirectLink(String redirectLink) {
        this.redirectLink = redirectLink;
    }

    public String getImageToDisplayRef() {
        return imageToDisplayRef;
    }

    public void setImageToDisplayRef(String imageToDisplayRef) {
        this.imageToDisplayRef = imageToDisplayRef;
    }

    public Date getStartRollDate() {
        return startRollDate;
    }

    public void setStartRollDate(Date startRollDate) {
        this.startRollDate = startRollDate;
    }

    public Date getEndRollDate() {
        return endRollDate;
    }

    public void setEndRollDate(Date endRollDate) {
        this.endRollDate = endRollDate;
    }

    public long getClickedCount() {
        return clickedCount;
    }

    public void setClickedCount(long clickedCount) {
        this.clickedCount = clickedCount;
    }

    public long getDisplayedCount() {
        return displayedCount;
    }

    public void setDisplayedCount(long displayedCount) {
        this.displayedCount = displayedCount;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public long getImageSize() {
        return imageSize;
    }

    public void setImageSize(long imageSize) {
        this.imageSize = imageSize;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
