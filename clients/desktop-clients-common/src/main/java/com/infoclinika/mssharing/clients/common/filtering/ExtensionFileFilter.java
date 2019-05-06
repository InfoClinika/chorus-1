// Copyright (c) 2016, NanoString Technologies, Inc.  All rights reserved.
// Use of this file for any purpose requires prior written consent of NanoString Technologies, Inc.

package com.infoclinika.mssharing.clients.common.filtering;

import com.infoclinika.util.FilenameUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yevhen Panko (yevhen.panko@teamdev.com)
 */
public class ExtensionFileFilter implements FileFilter {
    private final List<String> extensions = new ArrayList<>();
    private final List<String> fileNamesToExclude = new ArrayList<>();

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

    public void setFileNamesToExclude(String fileName) {
        fileNamesToExclude.add(fileName.toLowerCase());
    }

    @Override
    public boolean accept(File file) {
        final String name = file.getName();
        final String extension = FilenameUtil.getExtension(name);
        return !fileNamesToExclude.contains(name.toLowerCase()) &&
            extensions.contains(extension.toLowerCase());
    }
}
