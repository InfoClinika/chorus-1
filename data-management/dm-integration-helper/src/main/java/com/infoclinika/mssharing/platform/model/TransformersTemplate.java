package com.infoclinika.mssharing.platform.model;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.Dictionary;
import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.FileItem;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.common.items.NamedItem;

import java.util.Comparator;

/**
 * @author Herman Zamula
 */
public interface TransformersTemplate {

    Function<Dictionary, DictionaryItem> dictionaryItemTransformer();

    <T extends FileMetaDataTemplate> Function<T, FileItem> fileTransformer();

    <T extends InstrumentTemplate> Function<T, InstrumentItem> instrumentItemTransformer();

    Comparator<InstrumentItem> instrumentItemComparator();

    Comparator<? super DictionaryItem> dictionaryItemComparator();

    Comparator<? super NamedItem> namedItemComparator();
}
