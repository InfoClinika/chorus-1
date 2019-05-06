package com.infoclinika.mssharing.propertiesprovider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppearancePropertiesProvider extends AbstractPropertiesProvider {

    @Value("${appearance.links.about.show}")
    private boolean appearanceShowAboutLink;

    @Value("${appearance.links.blogs.show}")
    private boolean appearanceShowBlogsLink;

    @Value("${appearance.links.forum.show}")
    private boolean appearanceShowForumLink;

    @Value("${appearance.links.news.show}")
    private boolean appearanceShowNewsLink;

    @Value("${appearance.logo}")
    private String appearanceLogo;

    public boolean isAppearanceShowAboutLink() {
        return appearanceShowAboutLink;
    }

    public boolean isAppearanceShowBlogsLink() {
        return appearanceShowBlogsLink;
    }

    public boolean isAppearanceShowForumLink() {
        return appearanceShowForumLink;
    }

    public boolean isAppearanceShowNewsLink() {
        return appearanceShowNewsLink;
    }

    public String getAppearanceLogo() {
        return appearanceLogo;
    }
}
