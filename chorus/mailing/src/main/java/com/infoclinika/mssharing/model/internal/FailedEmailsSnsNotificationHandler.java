package com.infoclinika.mssharing.model.internal;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.infoclinika.mssharing.model.helper.FailedMailsHelper;
import com.infoclinika.mssharing.model.helper.FailedMailsHelper.FailedEmailItem;
import com.infoclinika.mssharing.model.internal.FailedEmailsSnsNotificationHandler.SnsBounceObject.Bounce;
import com.infoclinika.mssharing.model.internal.FailedEmailsSnsNotificationHandler.SnsBounceObject.Bounce.Recipients;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toSet;

/**
 * @author Herman Zamula
 */
@Component
public class FailedEmailsSnsNotificationHandler {

    private static final Logger LOG = Logger.getLogger(FailedEmailsSnsNotificationHandler.class);

    private final AmazonSQSClient sqsClient;
    private String queueUrl;
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private FailedMailsHelper failedMailsHelper;

    @Inject
    public FailedEmailsSnsNotificationHandler(@Value("${amazon.key}") String key,
                                              @Value("${amazon.secret}") String secret,
                                              @Value("${amazon.sqs.failed.emails.url}") String queueUrl,
                                              FailedMailsHelper failedMailsHelper
    ) {
        this.queueUrl = queueUrl;
        this.failedMailsHelper = failedMailsHelper;
        this.sqsClient = new AmazonSQSClient(new BasicAWSCredentials(key, secret));

    }

    public boolean handlingIsEnabled() {
        return !queueUrl.isEmpty();
    }

    private void handleBounce(SnsBounceObject snsBounceObject) {

        final Bounce bounce = checkNotNull(snsBounceObject.getBounce(), "Bounce object not found");
        final Set<FailedEmailItem> failedEmails = toFailedEmailItems(bounce.getBouncedRecipients());
        failedMailsHelper.handleFailedEmails(bounce.getBounceType(), bounce.getBounceSubType(), bounce.getTimestamp(), failedEmails, snsBounceObject.getRawJson());

    }

    private Set<FailedEmailItem> toFailedEmailItems(Set<Recipients> recipients) {
        return recipients.stream()
                .map((recipient) -> new FailedEmailItem(recipient.getEmailAddress(), recipient.getDiagnosticCode()))
                .collect(toSet());
    }

    public void handleMessages() {

        if( !handlingIsEnabled() ) {
            LOG.debug("Handling of emails is disabled");
            return;
        }
        LOG.debug("Start handling failed emails...");

        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        List<Message> result;

        int totalHandledMessages = 0;

        do {

            result = sqsClient.receiveMessage(receiveMessageRequest).getMessages();

            result.stream()
                    .map(this::parseSnsBounceObject)
                    .forEach(this::handleBounce);

            result.stream()
                    .forEach(message -> sqsClient.deleteMessage(queueUrl, message.getReceiptHandle()));

            totalHandledMessages += result.size();

        } while (!result.isEmpty());

        if (totalHandledMessages != 0) {
            LOG.info("Handling failed emails completed. Total handled messages count: " + totalHandledMessages);
        }
    }

    private SnsBounceObject parseSnsBounceObject(Message message) {
        try {

            final String rawBounce = checkNotNull(
                    jsonMapper.readValue(message.getBody(), HashMap.class).get("Message"),
                    "Message not found"
            ).toString();

            final SnsBounceObject bounceObject = jsonMapper.readValue(rawBounce, SnsBounceObject.class);

            bounceObject.setRawJson(rawBounce);

            return bounceObject;

        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SnsBounceObject {

        private String notificationType;
        private Bounce bounce;
        private Mail mail;
        @JsonIgnore
        private String rawJson;

        public String getNotificationType() {
            return notificationType;
        }

        public void setNotificationType(String notificationType) {
            this.notificationType = notificationType;
        }

        public Bounce getBounce() {
            return bounce;
        }

        public void setBounce(Bounce bounce) {
            this.bounce = bounce;
        }

        public Mail getMail() {
            return mail;
        }

        public void setMail(Mail mail) {
            this.mail = mail;
        }

        public String getRawJson() {
            return rawJson;
        }

        public void setRawJson(String rawJson) {
            this.rawJson = rawJson;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Bounce {

            private String bounceType;
            private String bounceSubType;
            private Set<Recipients> bouncedRecipients;
            private String timestamp;
            private String feedbackId;
            private String diagnosticCode;

            public String getBounceType() {
                return bounceType;
            }

            public void setBounceType(String bounceType) {
                this.bounceType = bounceType;
            }

            public String getBounceSubType() {
                return bounceSubType;
            }

            public void setBounceSubType(String bounceSubType) {
                this.bounceSubType = bounceSubType;
            }

            public Set<Recipients> getBouncedRecipients() {
                return bouncedRecipients;
            }

            public void setBouncedRecipients(Set<Recipients> bouncedRecipients) {
                this.bouncedRecipients = bouncedRecipients;
            }

            public String getTimestamp() {
                return timestamp;
            }

            public void setTimestamp(String timestamp) {
                this.timestamp = timestamp;
            }

            public String getFeedbackId() {
                return feedbackId;
            }

            public void setFeedbackId(String feedbackId) {
                this.feedbackId = feedbackId;
            }

            public String getDiagnosticCode() {
                return diagnosticCode;
            }

            public void setDiagnosticCode(String diagnosticCode) {
                this.diagnosticCode = diagnosticCode;
            }

            public static class Recipients {

                private String status;
                private String action;
                private String diagnosticCode;
                private String emailAddress;

                public String getEmailAddress() {
                    return emailAddress;
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (o == null || getClass() != o.getClass()) return false;

                    Recipients that = (Recipients) o;

                    return getEmailAddress() != null ? getEmailAddress().equals(that.getEmailAddress()) : that.getEmailAddress() == null;

                }

                @Override
                public int hashCode() {
                    return getEmailAddress() != null ? getEmailAddress().hashCode() : 0;
                }

                public String getStatus() {
                    return status;
                }

                public void setStatus(String status) {
                    this.status = status;
                }

                public String getAction() {
                    return action;
                }

                public void setAction(String action) {
                    this.action = action;
                }

                public String getDiagnosticCode() {
                    return diagnosticCode;
                }

                public void setDiagnosticCode(String diagnosticCode) {
                    this.diagnosticCode = diagnosticCode;
                }

                public void setEmailAddress(String emailAddress) {
                    this.emailAddress = emailAddress;
                }
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Mail {
            public String timestamp;
            public String messageId;
            public String source;
            public String sourceArn;
            public String sendingAccountId;
            public Set<String> destination;

        }


    }


}
