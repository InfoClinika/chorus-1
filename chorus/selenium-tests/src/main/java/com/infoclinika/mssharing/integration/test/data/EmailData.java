package com.infoclinika.mssharing.integration.test.data;

import javax.mail.Address;
import java.util.Date;

/**
 * @author Alexander Orlov
 */
public class EmailData {

    private String subject;
    private Address from;
    private Address to;
    private Date receivedDate;
    private Object messageBody;

    public EmailData(String subject, Address from, Address to, Date receivedDate, Object messageBody) {
        this.subject = subject;
        this.from = from;
        this.to = to;
        this.receivedDate = receivedDate;
        this.messageBody = messageBody;
    }

    public String getSubject() {
        return subject;
    }

    public String getFrom() {
        return from.toString();
    }

    public String getTo() {
        return to.toString();
    }

    public String getReceivedDate() {
        return receivedDate.toString();
    }

    public String getMessageBody() {
        return messageBody.toString();
    }
}
