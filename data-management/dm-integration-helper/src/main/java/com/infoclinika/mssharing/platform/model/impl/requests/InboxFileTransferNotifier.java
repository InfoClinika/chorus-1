package com.infoclinika.mssharing.platform.model.impl.requests;

import com.infoclinika.mssharing.platform.model.FileTransferNotifier;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Andrii Volynets on 7/7/2017.
 */
@Service("InboxFileTransferNotifier")
public class InboxFileTransferNotifier extends DefaultInboxNotifier implements FileTransferNotifier {

    @Override
    public void notifyFileTransferCompleted(long ownerId, long labId, List<DictionaryItem> files) {
        String message = files.size() == 1 ?
            String.format("File %s have been uploaded", files.get(0).name) :
            String.format("%s files have been uploaded", files.size());

        List<Long> labMemberIds = notifierHelper.getLabMemberIds(labId);
        for (Long memberId : labMemberIds) {
            notify(ownerId, memberId, message);
        }
    }
}
