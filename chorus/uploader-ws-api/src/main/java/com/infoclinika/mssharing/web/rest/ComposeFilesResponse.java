// Copyright (c) 2016, NanoString Technologies, Inc.  All rights reserved.
// Use of this file for any purpose requires prior written consent of NanoString Technologies, Inc.

package com.infoclinika.mssharing.web.rest;

import com.infoclinika.mssharing.dto.ComposedFileDescription;

/**
 * @author Yevhen Panko (yevhen.panko@teamdev.com)
 */
public class ComposeFilesResponse {
    public ComposedFileDescription[] composedFileDescriptions;

    public ComposeFilesResponse() {
    }

    public ComposeFilesResponse(ComposedFileDescription[] composedFileDescriptions) {
        this.composedFileDescriptions = composedFileDescriptions;
    }
}
