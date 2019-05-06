package com.infoclinika.mssharing.model.internal.helper;

import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.helper.FailedMailsHelper;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.mailing.FailedMailRecord;
import com.infoclinika.mssharing.model.internal.repository.FailedEmailsNotifierRepository;
import com.infoclinika.mssharing.model.internal.repository.FailedEmailsRecordRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

/**
 * @author Herman Zamula
 */
@Component
public class FailedMailsHelperImpl implements FailedMailsHelper {

    @Inject
    private FailedEmailsRecordRepository failedEmailsRecordRepository;
    @Inject
    private FailedEmailsNotifierRepository failedEmailsNotifierRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private Notifier notifier;

    @Override
    @Transactional
    public Set<Long> handleFailedEmails(
        String bounceType,
        String bounceSubType,
        String timestamp,
        Set<FailedEmailItem> failedEmails,
        String rawJson
    ) {

        final Set<Long> failedRecordsIds =
            saveFailedEmailRecords(bounceType, bounceSubType, timestamp, failedEmails, rawJson);

        sendEmailsSendingFailedNotifications(
            failedEmails.stream().map(e -> e.email).collect(toSet()),
            failedRecordsIds
        );

        return failedRecordsIds;

    }

    private Set<Long> saveFailedEmailRecords(String bounceType,
                                             String bounceSubType,
                                             String timestamp,
                                             Set<FailedEmailItem> failedEmails,
                                             String rawJson) {

        return failedEmails.stream()
            .map(failedEmailItem -> toFailedRecord(bounceType, bounceSubType, timestamp, failedEmailItem, rawJson))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .peek(this::skipEmailSending)
            .peek(failedEmailsRecordRepository::save)
            .map(FailedMailRecord::getId)
            .collect(toSet());

    }

    private Optional<FailedMailRecord> toFailedRecord(String bounceType,
                                                      String bounceSubType,
                                                      String timestamp,
                                                      FailedEmailItem failedEmailItem,
                                                      String rawJson) {
        final Optional<User> nullable = ofNullable(userRepository.findByEmail(failedEmailItem.email));
        return nullable.map(user -> new FailedMailRecord(user, bounceType, bounceSubType,
            timestamp, failedEmailItem.reason, rawJson, new Date()
        ));
    }

    private void skipEmailSending(FailedMailRecord failedMailRecord) {
        failedMailRecord.getUser().setSkipEmailsSending(true);
        userRepository.save(failedMailRecord.getUser());
    }

    private void sendEmailsSendingFailedNotifications(Set<String> failedEmails, Set<Long> failedRecordsIds) {

        failedEmailsNotifierRepository.findAll()
            .forEach(failedMailNotificationReceiver -> {
                notifier.sendFailedEmailsNotification(failedMailNotificationReceiver.getEmail(), failedEmails,
                    failedRecordsIds
                );
            });

    }


}
