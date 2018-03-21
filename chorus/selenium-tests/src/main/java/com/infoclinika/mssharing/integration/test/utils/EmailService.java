package com.infoclinika.mssharing.integration.test.utils;

import com.infoclinika.mssharing.integration.test.data.EmailData;
import com.infoclinika.mssharing.integration.test.data.EmailFolder;
import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.logging.Logger;
import com.sun.mail.imap.IMAPFolder;

import javax.mail.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.fail;

/**
 * @author Alexander Orlov
 *
 * Summary: This class provides service to connect to the gmail email box using IMAP. Email box of default testing user
 * has been choosed for connection. Appropriate filters have been created in his email box for convenient waiting of
 * each email.
 *
 */
public class EmailService {

    private static final String MAIL_STORE_PROTOCOL_PROPERTY = "mail.store.protocol";
    private static final String IMAPS = "imaps";
    private static final String HOST = "imap.googlemail.com";
    private static final UserData chorusTesterAtGmail = new UserData.Builder().email("chorus.tester@gmail.com").password("Password123456").build();
    private static final String GMAIL_TRASH_FOLDER = "[Gmail]/Trash";
    private static final String PARENT_FOLDER = "Chorus/";

    private static Store connectToEmailBox(UserData userData) {
        Properties properties = System.getProperties();
        properties.setProperty(MAIL_STORE_PROTOCOL_PROPERTY, IMAPS);
        Session session = Session.getDefaultInstance(properties, null);
        Store store = null;
        try {
            store = session.getStore(IMAPS);
            store.connect(HOST, userData.getEmail(), userData.getPassword());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return store;
    }

    private static IMAPFolder waitForEmailInFolder(Store store, String folderName) throws MessagingException {
        IMAPFolder chorusFolder;
        chorusFolder = (IMAPFolder) store.getFolder(PARENT_FOLDER + folderName);
        int seconds = 0;
        while (chorusFolder.getMessageCount() == 0) {
            chorusFolder = (IMAPFolder) store.getFolder(PARENT_FOLDER + folderName);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            seconds++;
            if (seconds == 3000) {
                fail("Email from Chorus has not been delivered after 3 min waiting");
                break;
            }
        }
        return chorusFolder;
    }

    private static Message[] getMessages(IMAPFolder folder) throws MessagingException, IOException {
        if (!folder.isOpen()) {
            folder.open(Folder.READ_WRITE);
        }
        Message[] messages = folder.getMessages();
        Logger.log("No of Messages : " + folder.getMessageCount());
        return messages;
    }

    private static List<EmailData> getEmailDataFromMessages(Message[] messages) throws MessagingException, IOException {
        List<EmailData> emailsList;
        emailsList = new ArrayList<EmailData>();
        for (int i = 0; i < messages.length; i++) {
            Logger.log("MESSAGE " + (i + 1) + ":");

            Message msg = messages[i];

            Logger.log("Subject: " + msg.getSubject());
            Logger.log("From: " + msg.getFrom()[0]);
            Logger.log("To: " + msg.getAllRecipients()[0]);
            Logger.log("Date: " + msg.getReceivedDate());
            Logger.log("Body: \n" + msg.getContent());
            emailsList.add(new EmailData(msg.getSubject(), msg.getFrom()[0], msg.getAllRecipients()[0], msg.getReceivedDate(), msg.getContent()));
        }
        return emailsList;
    }

    private static void removeAllMessages(Store store, Message[] messages, IMAPFolder folder) throws MessagingException {
        //remove all messages in the folder after reading them
        Folder trash = store.getFolder(GMAIL_TRASH_FOLDER);
        for (Message m : messages) {
            Logger.log("Removing message with subject " + m.getSubject() + "...");
            folder.copyMessages(new Message[]{m}, trash);
        }
    }

    private static void disconnectFromEmailBox(Store store, IMAPFolder folder) {
        if (folder != null && folder.isOpen()) {
            try {
                folder.close(true);
                if (store != null) {
                    store.close();
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    //Complex public methods
    public static List<EmailData> connectAndGetAllEmailsFromFolder(EmailFolder folderName) {
        Store store = connectToEmailBox(chorusTesterAtGmail);
        IMAPFolder folder = null;
        List<EmailData> emailsList = null;
        try {
            folder = waitForEmailInFolder(store, folderName.getName());
            Message[] messages = getMessages(folder);
            emailsList = getEmailDataFromMessages(messages);
            removeAllMessages(store, messages, folder);
        } catch (Exception e){
            fail("Error working with remote email service", e);
        } finally {
            disconnectFromEmailBox(store, folder);
        }
        return emailsList;
    }

    public static void waitForEmailAndRemoveAllMessagesFromFolder(EmailFolder folderName) {
        Store store = connectToEmailBox(chorusTesterAtGmail);
        IMAPFolder folder = null;
        try {
            folder = waitForEmailInFolder(store, folderName.getName());
            Message[] messages = getMessages(folder);
            removeAllMessages(store, messages, folder);
        } catch (Exception e) {
            fail("Error working with remote email service", e);
        } finally {
            disconnectFromEmailBox(store, folder);
        }
    }

    public static void connectAndRemoveAllMessagesFromFolder(EmailFolder folderName) {
        Store store = connectToEmailBox(chorusTesterAtGmail);
        IMAPFolder folder = null;
        try {
            folder = (IMAPFolder) store.getFolder(PARENT_FOLDER + folderName.getName());
            Message[] messages = getMessages(folder);
            removeAllMessages(store, messages, folder);
        } catch (Exception e){
            fail("Error working with remote email service", e);
        } finally {
            disconnectFromEmailBox(store, folder);
        }
    }

    public static String getLinkFromEmail(EmailData emailData) {
        String fullMessage = emailData.getMessageBody();
        String url = null;
        String regex = "(((http|https)://dev.chorusproject.org/)|((http|https)://chorusproject.org/)|(localhost:8080))(.+)(\")";       //finds link, which starts with https://dev.chorusproject.org/ and ends with double quites (including them)
        Matcher m = Pattern.compile(regex).matcher(fullMessage);
        if (m.find()) {
            url = m.group().replaceFirst("\"", ""); //removes double quotes from the link
        }
//        if (url != null && getChorusUrl().equals(localUrl)) {
//            int getIndexOfFirstSlash = url.indexOf("/", 7);
//            url = localUrl.substring(0, 28) + url.substring(getIndexOfFirstSlash);
//        }
        return url;
    }
}
