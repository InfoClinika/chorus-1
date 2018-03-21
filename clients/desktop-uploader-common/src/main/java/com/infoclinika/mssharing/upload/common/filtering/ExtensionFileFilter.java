// Copyright (c) 2016, NanoString Technologies, Inc.  All rights reserved.
// Use of this file for any purpose requires prior written consent of NanoString Technologies, Inc.

package com.infoclinika.mssharing.upload.common.filtering;

import com.infoclinika.mssharing.web.util.FilenameUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yevhen Panko (yevhen.panko@teamdev.com)
 */
public class ExtensionFileFilter implements FileFilter {
    private final List<String> extensions = new ArrayList<>();

    public ExtensionFileFilter() {
    }

    public void setExtensions(List<String> extensions) {
        if (extensions != null) {
            this.extensions.clear();
            for (String extension : extensions) {
                this.extensions.add(extension.toLowerCase());
            }
        }
    }

    @Override
    public boolean accept(File file) {
        final String extension = FilenameUtil.getExtension(file.getName());

        return extensions.contains(extension.toLowerCase());
    }
}
