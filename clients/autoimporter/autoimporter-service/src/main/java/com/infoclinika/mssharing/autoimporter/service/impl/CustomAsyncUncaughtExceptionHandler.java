package com.infoclinika.mssharing.autoimporter.service.impl;

import com.infoclinika.mssharing.autoimporter.service.api.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author timofei.kasianov 8/29/18
 */
public class CustomAsyncUncaughtExceptionHandler extends SimpleAsyncUncaughtExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAsyncUncaughtExceptionHandler.class);

    private final List<ExceptionHandler> exceptionHandlers;

    public CustomAsyncUncaughtExceptionHandler(List<ExceptionHandler> exceptionHandlers) {
        this.exceptionHandlers = exceptionHandlers;
    }

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        for (ExceptionHandler exceptionHandler : exceptionHandlers) {
            try {
                if (exceptionHandler.canHandle(throwable.getClass())) {
                    exceptionHandler.handle(throwable);
                }
            } catch (Throwable e) {
                LOGGER.warn("Unable to handle uncaught exception. Ignoring.", e);
            }
        }
    }

}
