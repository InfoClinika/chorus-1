package com.infoclinika.mssharing.web.rest.exception;

import com.google.gson.Gson;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.model.UploadLimitException;
import com.infoclinika.mssharing.model.UploadUnavailable;
import com.infoclinika.mssharing.web.rest.RestExceptionType;
import org.apache.log4j.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * @author timofey.kasyanov
 *         date: 07.05.2014
 */
@Provider
public class RestExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = Logger.getLogger(RestExceptionMapper.class);

    private final Map<Class<? extends Exception>, RestExceptionType> exceptionTypeMap = newHashMap();
    private final RestExceptionType defaultExceptionType = RestExceptionType.SERVER_ERROR;
    private final Gson gson = new Gson();

    {
        exceptionTypeMap.put(AccessDenied.class, RestExceptionType.ACCESS_DENIED);
        exceptionTypeMap.put(UploadLimitException.class, RestExceptionType.UPLOAD_LIMIT_EXCEEDED);
        exceptionTypeMap.put(UploadUnavailable.class, RestExceptionType.UPLOAD_UNAVAILABLE);
        exceptionTypeMap.put(BadCredentialsException.class, RestExceptionType.BAD_CREDENTIALS);
    }

    @Override
    public Response toResponse(Exception exception) {

        final Class<? extends Exception> exceptionClass = exception.getClass();

        LOGGER.debug(" *** Exception occurred. Exception class: " + exceptionClass);

        final RestExceptionType restExceptionType = exceptionTypeMap.containsKey(exceptionClass) ?
                exceptionTypeMap.get(exceptionClass) :
                defaultExceptionType;
        final RestExceptionResponse responseEntity = new RestExceptionResponse(restExceptionType, exception.getMessage());

        return Response
                .serverError()
                .entity(gson.toJson(responseEntity))
                .build();
    }

    public static class RestExceptionResponse {
        private RestExceptionType type;
        private String message;

        public RestExceptionResponse(RestExceptionType type, String message) {
            this.type = type;
            this.message = message;
        }

        public RestExceptionType getType() {
            return type;
        }

        public void setType(RestExceptionType type) {
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
