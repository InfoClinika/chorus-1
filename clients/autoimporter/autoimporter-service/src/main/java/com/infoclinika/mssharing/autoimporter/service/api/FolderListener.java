package com.infoclinika.mssharing.autoimporter.service.api;

import com.infoclinika.mssharing.autoimporter.model.Context;
import org.apache.commons.io.monitor.FileAlterationListener;

/**
 * author Ruslan Duboveckij
 */
public interface FolderListener extends FileAlterationListener, DefaultInitUtil<Context> {
}
