package com.infoclinika.mssharing.model.write;

import java.io.File;
import java.net.URL;

/**
 * @author Elena Kurilina
 */
public interface LogUploader {

    public URL uploadFile(File file);
}
