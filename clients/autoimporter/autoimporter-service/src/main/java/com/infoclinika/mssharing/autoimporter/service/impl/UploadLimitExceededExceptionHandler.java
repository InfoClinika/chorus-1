package com.infoclinika.mssharing.autoimporter.service.impl;

import com.infoclinika.mssharing.autoimporter.service.api.AppEventNotifier;
import com.infoclinika.mssharing.autoimporter.service.api.ExceptionHandler;
import com.infoclinika.mssharing.clients.common.web.api.exception.UploadLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author timofey.kasyanov
 *     date: 08.05.2014
 */
@Component
@Qualifier("uploadLimitExceededExceptionHandler")
public class UploadLimitExceededExceptionHandler implements ExceptionHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger(UploadLimitExceededExceptionHandler.class);

    @Inject
    private AppEventNotifier appEventNotifier;

    @Override
    public void handle(Throwable ex) {
        LOGGER.error(" *** Handle UploadLimitExceededException");
        appEventNotifier.notify(AppEventNotifier.AppEventCode.UPLOAD_LIMIT_ERROR);
    }

    @Override
    public boolean canHandle(Class<? extends Throwable> exceptionClass) {
        return UploadLimitExceededException.class.equals(exceptionClass);
    }
}
