package com.infoclinika.mssharing.fileserver.model;


import com.infoclinika.mssharing.platform.fileserver.StoredObject;

import java.io.FilterInputStream;

/**
 * @author Herman Zamula
 */
public class ArchivedFile implements StoredObject {

    private final FilterInputStream inputStream;

    public ArchivedFile(FilterInputStream inputStream) {
        this.inputStream = inputStream;
    }

    public FilterInputStream getInputStream() {
        return inputStream;
    }

    @Override
    public String toString() {
        return "StoredFile{" +
            "inputStream=" + inputStream +
            '}';
    }
}
