// Copyright (c) 2016, NanoString Technologies, Inc.  All rights reserved.
// Use of this file for any purpose requires prior written consent of NanoString Technologies, Inc.

package com.infoclinika.mssharing.web.rest;

import com.infoclinika.mssharing.dto.FileDescription;

/**
 * @author Yevhen Panko (yevhen.panko@teamdev.com)
 */
public class ComposeFilesRequest {
    public long instrumentId;
    public FileDescription[] fileDescriptions;

    public ComposeFilesRequest() {
    }

    public ComposeFilesRequest(long instrumentId, FileDescription[] fileDescriptions) {
        this.instrumentId = instrumentId;
        this.fileDescriptions = fileDescriptions;
    }
}
