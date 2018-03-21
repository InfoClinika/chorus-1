package com.infoclinika.mssharing.platform.model.helper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.FileItem;

/**
 * @author Herman Zamula
 */
public interface UploadHelperTemplate {

    ImmutableSet<DictionaryItem> species();

    ImmutableSortedSet<FileItem> existingFiles(long actor, long instrument);

}
