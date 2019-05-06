/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.fileserver.model;

import com.infoclinika.mssharing.platform.fileserver.StoredObject;

import java.io.InputStream;

/**
 * @author Oleksii Tymchenko
 */
public class StoredFile implements StoredObject {
    private final InputStream inputStream;
    //Let setting the size of the stream to avoid memory issues during upload
    private Long size;

    public StoredFile(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "StoredFile{" +
                "inputStream=" + inputStream +
                ", size=" + size +
                '}';
    }
}
