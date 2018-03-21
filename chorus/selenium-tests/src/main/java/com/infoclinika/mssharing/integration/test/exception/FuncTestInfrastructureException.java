package com.infoclinika.mssharing.integration.test.exception;

/**
 * @author Alexander Orlov
 */
public class FuncTestInfrastructureException extends RuntimeException{

    public FuncTestInfrastructureException() {
        super();
    }

    public FuncTestInfrastructureException(String s) {
        super(s);
    }

    public FuncTestInfrastructureException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public FuncTestInfrastructureException(Throwable throwable) {
        super(throwable);
    }
}
