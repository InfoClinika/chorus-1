package com.infoclinika.mssharing.model.read;

/**
* @author Herman Zamula
*/
public class FileUserFunctionInfoItem {

    public final long lab;
    public final long initiator;

    public FileUserFunctionInfoItem(long lab, long initiator) {
        this.lab = lab;
        this.initiator = initiator;
    }
}
