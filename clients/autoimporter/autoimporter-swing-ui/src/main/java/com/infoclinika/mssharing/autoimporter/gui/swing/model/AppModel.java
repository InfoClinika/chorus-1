package com.infoclinika.mssharing.autoimporter.gui.swing.model;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.autoimporter.model.bean.ContextInfo;
import org.springframework.stereotype.Service;

/**
 * @author Ruslan Duboveckij
 */
@Service
public class AppModel {
    private Optional<ContextInfo> contextInfo = Optional.absent();

    public Optional<ContextInfo> getContextInfo() {
        return contextInfo;
    }

    public void setContextInfo(ContextInfo contextInfo) {
        this.contextInfo = Optional.fromNullable(contextInfo);
    }

    public String getFolder() {
        return contextInfo.isPresent() ? contextInfo.get().getFolder() : "";
    }

    public boolean isStarted() {
        return contextInfo.isPresent() && contextInfo.get().isStarted();
    }

    public String getName() {
        return contextInfo.isPresent() ? contextInfo.get().getName() : "";
    }

    public String getLabels() {
        return contextInfo.isPresent() ? contextInfo.get().getLabels() : "";
    }
}
