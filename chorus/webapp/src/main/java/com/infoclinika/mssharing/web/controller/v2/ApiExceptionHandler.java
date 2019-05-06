package com.infoclinika.mssharing.web.controller.v2;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.ws.rs.*;

/**
 * @author Vitalii Petkanych
 */
@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ApiException.class})
    public ResponseEntity<ApiExceptionDTO> handleGeneralApiError(ApiException e, WebRequest request) {
        final String message = e.getCause() instanceof AmazonS3Exception
            ? ((AmazonS3Exception) e.getCause()).getErrorMessage()
            : e.getLocalizedMessage();
        return new ResponseEntity<>(new ApiExceptionDTO(message), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler({NotFoundException.class, ForbiddenException.class})
    public ResponseEntity<ApiExceptionDTO> handleGeneralApiError(ClientErrorException e, WebRequest request) {
        return e instanceof NotFoundException
            ? new ResponseEntity<>(new ApiExceptionDTO("Entity not found"), HttpStatus.NOT_FOUND)
            : new ResponseEntity<>(new ApiExceptionDTO("Forbidden"), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({InternalServerErrorException.class})
    public ResponseEntity<ApiExceptionDTO> handleGeneralApiError(ServerErrorException e, WebRequest request) {
        return new ResponseEntity<ApiExceptionDTO>(
            new ApiExceptionDTO(e.getMessage()),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    class ApiExceptionDTO {
        private String message;

        public ApiExceptionDTO(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
