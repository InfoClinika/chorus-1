package com.infoclinika.mssharing.model.helper;

import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author Herman Zamula
 */
@Transactional
public interface FileMetaInfoHelper {

    void updateFileMeta(long fileId, MetaInfo metaInfo);
    void copyFileMetaAnnotation(long to, long from);

    static class MetaInfo {
        public Date creationDate;
        public String comment;
        public String instrumentMethod;
        public String startRt;
        public String endRt;
        public String startMz;
        public String endMz;
        public String fileName;
        public String seqRowPosition;
        public String sampleName;
        public String instrument;
        public String userName;

        public String userLabels;

        public String fileCondition;
        public String translateFlag;
        public String instrumentSerialNumber;
        public String phone;
        public String instrumentName;

        public MetaInfo( ){}

        public MetaInfo(Date creationDate, String comment, String instrumentMethod, String fileName, String seqRowPosition, String sampleName, String instrument, String userName, String userLabels, String fileCondition, String translateFlag, String instrumentSerialNumber, String phone, String instrumentName) {
            this.creationDate = creationDate;
            this.comment = comment;
            this.instrumentMethod = instrumentMethod;
            this.fileName = fileName;
            this.seqRowPosition = seqRowPosition;
            this.sampleName = sampleName;
            this.instrument = instrument;
            this.userName = userName;
            this.userLabels = userLabels;
            this.fileCondition = fileCondition;
            this.translateFlag = translateFlag;
            this.instrumentSerialNumber = instrumentSerialNumber;
            this.phone = phone;
            this.instrumentName = instrumentName;
        }
    }
}
