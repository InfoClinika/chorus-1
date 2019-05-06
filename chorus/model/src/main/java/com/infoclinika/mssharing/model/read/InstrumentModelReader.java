package com.infoclinika.mssharing.model.read;

import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.read.InstrumentModelReaderTemplate.InstrumentModelLineTemplate;

import java.util.Set;

/**
 * @author timofei.kasianov 12/8/16
 */
public interface InstrumentModelReader {

    PagedItem<InstrumentModelLine> read(long actor, PagedItemInfo pagedItem);

    boolean isNameUnique(long actor, String name, Long vendor, Long model);

    class InstrumentModelLine extends InstrumentModelLineTemplate {

        public final long instrumentsCount;

        public InstrumentModelLine(long id, String name, DictionaryItem technologyType,
                                   DictionaryItem vendor, DictionaryItem instrumentType,
                                   Set<String> extensions, boolean additionalFiles,
                                   boolean folderArchiveSupport, long instrumentsCount) {
            super(id, name, technologyType, vendor, instrumentType, extensions, additionalFiles, folderArchiveSupport);
            this.instrumentsCount = instrumentsCount;
        }
    }

}
