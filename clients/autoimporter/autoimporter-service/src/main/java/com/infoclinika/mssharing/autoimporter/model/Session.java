package com.infoclinika.mssharing.autoimporter.model;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.infoclinika.mssharing.autoimporter.model.bean.ContextInfo;
import com.infoclinika.mssharing.dto.request.UserNamePassDTO;
import com.infoclinika.mssharing.dto.response.AuthenticateDTO;
import com.infoclinika.mssharing.dto.response.UploadConfigDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * author Ruslan Duboveckij
 */
public abstract class Session {

    private static final Logger LOGGER = LoggerFactory.getLogger(Session.class);

    private Map<String, Context> contexts = Maps.newConcurrentMap();
    private AuthenticateDTO authenticate;
    private AWSCredentials credentials;
    private UserNamePassDTO appCredentials;
    private String clientToken;

    protected abstract Context createContext();

    public AWSCredentials getCredentials() {
        return credentials;
    }

    public Map<String, Context> getContexts() {
        return contexts;
    }

    public Context getContext(String folder) {
        return contexts.get(folder);
    }

    public Context removeContext(String folder) {
        return contexts.remove(folder);
    }

    public void addContext(ContextInfo info) {
        //avoid several contexts for the same folder
        if (!contexts.containsKey(info.getFolder())) {

            final Context context = createContext();

            context.init(info);
            contexts.put(info.getFolder(), context);

            LOGGER.info("Context added to the session. Context folder: {}", info.getFolder());

        } else {

            LOGGER.info("Context has already been added. Context folder: {}", info.getFolder());

        }
    }

    public AuthenticateDTO getAuthenticate() {
        return authenticate;
    }

    public void setAuthenticate(AuthenticateDTO authenticate) {

        final UploadConfigDTO uploadConfig = authenticate.getUploadConfig();
        if (uploadConfig.isUseRoles()) {
            credentials = new BasicSessionCredentials(uploadConfig.getAmazonKey(), uploadConfig.getAmazonSecret(),
                uploadConfig.getSessionToken()
            );
        } else {
            credentials = new BasicAWSCredentials(uploadConfig.getAmazonKey(), uploadConfig.getAmazonSecret());
        }
        this.authenticate = authenticate;
    }

    public void clear() {
        contexts.clear();
        authenticate = new AuthenticateDTO("", "", null);
        appCredentials = new UserNamePassDTO("", "");
        clientToken = "";
    }

    public UserNamePassDTO getAppCredentials() {
        return appCredentials;
    }

    public void setAppCredentials(UserNamePassDTO appCredentials) {
        this.appCredentials = appCredentials;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("contexts", contexts)
            .add("authenticate", authenticate)
            .add("credentials", credentials)
            .toString();
    }
}
