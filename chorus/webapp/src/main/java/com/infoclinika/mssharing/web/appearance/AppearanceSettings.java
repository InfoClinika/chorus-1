package com.infoclinika.mssharing.web.appearance;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author : Alexander Serebriyan
 */
@Component
public class AppearanceSettings {

    @Value("${appearance.links.forum.show}")
    private boolean showForumLink;

    @Value("${appearance.links.news.show}")
    private boolean showNewsLink;

    @Value("${appearance.links.blogs.show}")
    private boolean showBlogsLink;

    @Value("${appearance.links.about.show}")
    private boolean showAboutLink;

    @Value("${appearance.logo}")
    private String logo;

    public boolean isShowForumLink() {
        return showForumLink;
    }

    public void setShowForumLink(boolean showForumLink) {
        this.showForumLink = showForumLink;
    }

    public boolean isShowNewsLink() {
        return showNewsLink;
    }

    public void setShowNewsLink(boolean showNewsLink) {
        this.showNewsLink = showNewsLink;
    }

    public boolean isShowBlogsLink() {
        return showBlogsLink;
    }

    public void setShowBlogsLink(boolean showBlogsLink) {
        this.showBlogsLink = showBlogsLink;
    }

    public boolean isShowAboutLink() {
        return showAboutLink;
    }

    public void setShowAboutLink(boolean showAboutLink) {
        this.showAboutLink = showAboutLink;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
