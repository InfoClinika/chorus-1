/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.web.controller.response;

import java.util.List;

/**
 * @author Oleksii Tymchenko
 */
public class UploadFilesResponse {
    public final long instrument;
    public final List<UploadFileResponseLine> files;

    public UploadFilesResponse(long instrument, List<UploadFileResponseLine> files) {
        this.instrument = instrument;
        this.files = files;
    }

    public static class UploadFileResponseLine {
        public final String name;
        public final long storedItemId;
        public final String destinationPath;

        public UploadFileResponseLine(String name, long storedItemId, String destinationPath) {
            this.name = name;
            this.storedItemId = storedItemId;
            this.destinationPath = destinationPath;
        }
    }
}
