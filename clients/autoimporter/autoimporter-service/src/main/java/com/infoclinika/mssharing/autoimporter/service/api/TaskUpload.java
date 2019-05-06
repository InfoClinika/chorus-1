package com.infoclinika.mssharing.autoimporter.service.api;

import com.infoclinika.mssharing.autoimporter.model.Context;

import java.io.File;
import java.util.List;

/**
 * author Ruslan Duboveckij
 */
public interface TaskUpload {
    public void start(Context context, List<File> files);
}
