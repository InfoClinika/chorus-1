package com.infoclinika.mssahring.model.internal.test;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.google.common.io.Resources;
import com.infoclinika.mssharing.model.helper.FailedMailsHelper;
import com.infoclinika.mssharing.model.internal.FailedEmailsSnsNotificationHandler;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.google.common.collect.ImmutableSet.of;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.never;
import static org.testng.Assert.assertFalse;

/**
 * @author Herman Zamula
 */
public class FailedEmalsSnsTest {

    private final FailedMailsHelper failedMailsHelper = Mockito.mock(FailedMailsHelper.class);

    private final String key = "AKIAIKSRDYUYOD4OD2NQ";
    private final String secret = "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";
    private final String queueUrl = "https://sqs.us-east-1.amazonaws.com/312091580521/chorus-test-bounce-emails";

    private final FailedEmailsSnsNotificationHandler failedEmailsSnsNotificationHandler =
        new FailedEmailsSnsNotificationHandler(new AmazonPropertiesProvider(), failedMailsHelper);

    //    @BeforeMethod
    public void initMessage() throws IOException {
        Mockito.reset(failedMailsHelper);

        AmazonSQSClient sqsClient = new AmazonSQSClient(new BasicAWSCredentials(key, secret));
        final URL resource = Resources.getResource("bouncedEmail.json");
        final String body = Resources.toString(resource, StandardCharsets.UTF_8);
        sqsClient.sendMessage(queueUrl, body);
    }

    //    @Test
    public void testShouldNotHandle() throws Exception {
        final String queueUrlEmpty = "";

        final FailedEmailsSnsNotificationHandler handler = new FailedEmailsSnsNotificationHandler(
            new AmazonPropertiesProvider(),
            failedMailsHelper
        );

        assertFalse(handler.handlingIsEnabled());

        handler.handleMessages();

        Mockito.verify(failedMailsHelper, never()).handleFailedEmails(anyString(), anyString(), anyString(),
            any(), anyString()
        );

    }

    //    @Test(enabled = false/*todo: investigate behavior, fails when run all tests*/)
    public void testHandingFailedEmails() {

        failedEmailsSnsNotificationHandler.handleMessages();

        Mockito.verify(failedMailsHelper)
            .handleFailedEmails(eq("Permanent"), eq("General"), eq("2015-12-18T10:08:23.556Z"),

                eq(of(new FailedMailsHelper.FailedEmailItem(
                    "herman.zamua@gmail.com",

                    "smtp; 550-5.1.1 The email account that you tried to reach does not exist. Please try\n" +
                        "550-5.1.1 double-checking the recipient's email address for typos or\n" +
                        "550-5.1.1 unnecessary spaces. Learn more at\n" +
                        "550 5.1.1  https://support.google.com/mail/answer/6596 a97si15572493qkh.49 - gsmtp"
                ))),

                eq("{\"notificationType\":\"Bounce\",\"bounce\":{\"bounceSubType\":\"General\"," +
                    "\"bounceType\":\"Permanent\",\"reportingMTA\":\"dsn; a8-24.smtp-out.amazonses.com\"," +
                    "\"bouncedRecipients\":" +
                    "[{\"emailAddress\":\"herman.zamua@gmail.com\",\"status\":\"5.1.1\",\"diagnosticCode\":\"smtp;" +
                    " 550-5.1.1 The email account that you tried to reach does not exist. Please try\\n550-5.1.1" +
                    " double-checking the recipient's email address for typos or\\n550-5.1.1 unnecessary spaces. " +
                    "Learn more at\\n550 5.1.1" +
                    "  https://support.google.com/mail/answer/6596 a97si15572493qkh.49 - gsmtp\"," +
                    "\"action\":\"failed\"}],\"timestamp\":" +
                    "\"2015-12-18T10:08:23.556Z\",\"feedbackId\":\"00000151b48fd38d-c65327a4-5136-4839-a803" +
                    "-66ec8b4248fe-000000\"},\"mail\"" +
                    ":{\"timestamp\":\"2015-12-18T10:08:22.000Z\",\"source\":\"support@chorusproject.org\"," +
                    "\"messageId\"" +
                    ":\"00000151b48fd0fb-4375397c-2f23-4780-92cd-c5d6b4f115fe-000000\",\"destination\":[\"herman" +
                    ".zamua@gmail.com\"]," +
                    "\"sendingAccountId\":\"312091580521\"," +
                    "\"sourceArn\":\"arn:aws:ses:us-east-1:312091580521:identity/support@chorusproject.org\"}}")

            );

    }

}
