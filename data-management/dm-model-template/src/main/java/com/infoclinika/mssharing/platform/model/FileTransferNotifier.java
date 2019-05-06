package com.infoclinika.mssharing.platform.model;

import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;

import java.util.List;

/**
 * Created by Volynets Andrii on 7/7/2017.
 */
public interface FileTransferNotifier {

    void notifyFileTransferCompleted(long ownerId, long labId, List<DictionaryItem> files);
}
