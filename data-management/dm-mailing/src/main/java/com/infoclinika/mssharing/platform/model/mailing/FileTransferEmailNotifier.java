package com.infoclinika.mssharing.platform.model.mailing;

import com.infoclinika.mssharing.platform.model.FileTransferNotifier;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by Andrii Volynets on 7/7/2017.
 */
@Component("FileTransferEmailNotifier")
public class FileTransferEmailNotifier extends DefaultEmailNotifier implements FileTransferNotifier {

    @Override
    public void notifyFileTransferCompleted(long ownerId, long labId, List<DictionaryItem> files) {
        final Map<String, Object> model = new HashMap<>();
        model.put("fileNames", files.stream().map(file -> file.name).collect(toList()));
        model.put("laboratory", mailSendingHelper.labName(labId));
        final List<String> emails = mailSendingHelper.labMembersEmails(labId);
        final Email email = prepareEmail(getTemplateLocation("fileUploadCompleted.vm"), model);
        emailer.send(emails, email.subject, email.message);
    }
}
