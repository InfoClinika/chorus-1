package com.infoclinika.mssharing.autoimporter.service.api;

/**
 * @author timofey.kasyanov
 *     date: 08.05.2014
 */
public interface ExceptionHandler {

    boolean canHandle(Class<? extends Throwable> exceptionClass);

    void handle(Throwable ex);
}
