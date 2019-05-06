package com.infoclinika.mssharing.parser;

import java.io.File;

/**
 * Created by davemakhervaks on 7/5/17.
 */
public class QueueAttribute {

    public File file;
    public int index;

    public QueueAttribute(File f, int i) {
        file = f;
        index = i;
    }
}
