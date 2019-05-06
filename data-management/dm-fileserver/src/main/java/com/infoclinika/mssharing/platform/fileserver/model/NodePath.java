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

import java.util.Objects;

/**
 * @author Oleksii Tymchenko
 */
public class NodePath {

    private final String bucket;
    private final String path;

    public NodePath(String path) {
        this(null, path);
    }

    public NodePath(String bucket, String path) {
        this.bucket = bucket;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getBucket() {
        return bucket;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodePath nodePath = (NodePath) o;
        return Objects.equals(bucket, nodePath.bucket) &&
                Objects.equals(path, nodePath.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bucket, path);
    }

    @Override
    public String toString() {
        return "NodePath{" +
                "bucket='" + bucket + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
