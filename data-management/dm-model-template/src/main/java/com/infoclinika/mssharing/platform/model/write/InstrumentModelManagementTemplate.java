package com.infoclinika.mssharing.platform.model.write;

import java.util.Set;

/**
 * @author timofei.kasianov 12/6/16
 */
public interface InstrumentModelManagementTemplate<
    DETAILS extends InstrumentModelManagementTemplate.InstrumentModelDetails> {

    long create(long actor, DETAILS details);

    void update(long actor, long modelId, DETAILS details);

    void delete(long actor, long modelId);

    class InstrumentModelDetails {
        public final String name;
        public final String technologyType;
        public final String vendor;
        public final String instrumentType;
        public final Set<String> extensions;
        public final boolean additionalFiles;
        public final boolean folderArchiveSupport;

        public InstrumentModelDetails(String name,
                                      String technologyType,
                                      String vendor,
                                      String instrumentType,
                                      Set<String> extensions) {
            this.name = name;
            this.technologyType = technologyType;
            this.vendor = vendor;
            this.instrumentType = instrumentType;
            this.extensions = extensions;
            this.additionalFiles = false;
            this.folderArchiveSupport = false;
        }

        public InstrumentModelDetails(String name, String technologyType, String vendor, String instrumentType,
                                      Set<String> extensions, boolean additionalFiles, boolean folderArchiveSupport) {
            this.name = name;
            this.technologyType = technologyType;
            this.vendor = vendor;
            this.instrumentType = instrumentType;
            this.extensions = extensions;
            this.additionalFiles = additionalFiles;
            this.folderArchiveSupport = folderArchiveSupport;
        }
    }
}
