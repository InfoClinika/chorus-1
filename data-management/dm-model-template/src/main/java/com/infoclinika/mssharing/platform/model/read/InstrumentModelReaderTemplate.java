package com.infoclinika.mssharing.platform.model.read;

import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;

import java.util.Set;

/**
 * @author timofei.kasianov 12/6/16
 */
public interface InstrumentModelReaderTemplate<MODEL_LINE extends InstrumentModelReaderTemplate.InstrumentModelLineTemplate> {

    MODEL_LINE readById(long actor, long modelId);

    Set<MODEL_LINE> readByVendor(long actor, long vendorId);

    Set<MODEL_LINE> readByStudyType(long actor, long typeId);

    Set<MODEL_LINE> readByStudyTypeAndVendor(long actor, long typeId, long vendorId);

    PagedItem<MODEL_LINE> readInstrumentModels(long actor, PagedItemInfo pagedItem);

    class InstrumentModelLineTemplate {
        public final long id;
        public final String name;
        public final DictionaryItem technologyType;
        public final DictionaryItem vendor;
        public final DictionaryItem instrumentType;
        public final Set<String> extensions;
        public final boolean additionalFiles;
        public final boolean folderArchiveSupport;

        public InstrumentModelLineTemplate(long id, String name, DictionaryItem technologyType, DictionaryItem vendor,
                                           DictionaryItem instrumentType, Set<String> extensions,
                                           boolean additionalFiles, boolean folderArchiveSupport) {
            this.id = id;
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
