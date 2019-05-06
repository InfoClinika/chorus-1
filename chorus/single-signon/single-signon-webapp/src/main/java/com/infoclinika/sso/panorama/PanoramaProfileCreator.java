package com.infoclinika.sso.panorama;

import com.infoclinika.sso.UserCredentials;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.http.profile.creator.ProfileCreator;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * @author andrii.loboda
 */
@Service
public final class PanoramaProfileCreator implements ProfileCreator<UserCredentials, CommonProfile> {

    @Override
    public CommonProfile create(UserCredentials credentials) {
        final CommonProfile profile = new CommonProfile();
        final Map<String, Object> attributes = newHashMap();
        profile.build(credentials.getUsername(), attributes);

        return profile;
    }
}
