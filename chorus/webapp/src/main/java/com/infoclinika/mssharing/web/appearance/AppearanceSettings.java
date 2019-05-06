package com.infoclinika.mssharing.web.appearance;

import com.infoclinika.mssharing.propertiesprovider.AppearancePropertiesProvider;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author : Alexander Serebriyan
 */
@Component
public class AppearanceSettings {

    @Inject
    AppearancePropertiesProvider appearancePropertiesProvider;

    public boolean isShowForumLink() {
        return appearancePropertiesProvider.isAppearanceShowForumLink();
    }

    public boolean isShowNewsLink() {
        return appearancePropertiesProvider.isAppearanceShowNewsLink();
    }

    public boolean isShowBlogsLink() {
        return appearancePropertiesProvider.isAppearanceShowBlogsLink();
    }

    public boolean isShowAboutLink() {
        return appearancePropertiesProvider.isAppearanceShowAboutLink();
    }

    public String getLogo() {
        return appearancePropertiesProvider.getAppearanceLogo();
    }
}
