package com.infoclinika.mssharing.clients.common.web.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoclinika.mssharing.dto.request.UploadFilesDTORequest;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author timofey.kasyanov
 *     date: 27.03.14.
 */
@Produces(MediaType.APPLICATION_JSON)
public class UploadRequestWriter implements MessageBodyWriter<UploadFilesDTORequest> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(UploadFilesDTORequest.class);
    }

    @Override
    public long getSize(UploadFilesDTORequest uploadFilesDTORequest, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(UploadFilesDTORequest uploadFilesDTORequest, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        objectMapper.writeValue(entityStream, uploadFilesDTORequest);
    }
}
