package com.infoclinika.mssharing.model.test.jira;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.AttachmentInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.internal.jira.JiraRestClientProvider;
import com.infoclinika.mssharing.model.jira.JiraService;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate.PersonInfo;
import com.infoclinika.mssharing.propertiesprovider.JiraPropertiesProvider;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static com.atlassian.util.concurrent.Promises.promise;
import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.model.jira.JiraService.IssueAttachment;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Vladislav Kovchug
 */
public class JiraServiceTest extends AbstractTest {

    private static final String TEST_ISSUE_DESCRIPTION = "Test issue description";
    private static final String TEST_ISSUE_SUMMARY = "Test issue summary";
    private static final Long TEST_ISSUE_PRIORITY = 2L;
    private static final Long TEST_ISSUE_TYPE = 1L;
    private static final String TEST_ISSUE_URL = "http://jira.com/issue/CP-1234";
    private static final String TEST_ISSUE_KEY = "CP-1234";
    private static final long TEST_ISSUE_ID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraServiceTest.class);

    @Inject
    private JiraRestClientProvider jiraRestClientProvider;

    @Inject
    private JiraService jiraService;

    @Inject
    private JiraPropertiesProvider jiraPropertiesProvider;

    @Test
    public void createIssue() throws URISyntaxException {
        final long bob = uc.createLab3AndBob();
        String issueDescription = "Reported by: " + getReporter(bob) + "\n\n" + TEST_ISSUE_DESCRIPTION;

        Mockito.reset(jiraRestClientProvider);
        final IssueRestClient issueRestClient = verifyIssueDescription(jiraRestClientProvider, issueDescription);

        jiraService.reportIssue(bob, TEST_ISSUE_TYPE, TEST_ISSUE_PRIORITY, TEST_ISSUE_SUMMARY, TEST_ISSUE_DESCRIPTION,
                                null, new ArrayList<>()
        );
        verify(issueRestClient, times(1)).createIssue(any());
    }

    @Test
    public void createIssueWithStepsToReproduce() throws URISyntaxException {
        final long bob = uc.createLab3AndBob();

        final String stepsToReproduce = "step1,\nstep2,\nstep3";
        final String issueDescription = "Reported by: " + getReporter(bob) + "\n\n" + TEST_ISSUE_DESCRIPTION +
            "\n\nSteps to reproduce: \n" + stepsToReproduce + "\n\n";

        Mockito.reset(jiraRestClientProvider);
        final IssueRestClient issueRestClient = verifyIssueDescription(jiraRestClientProvider, issueDescription);

        jiraService.reportIssue(bob, TEST_ISSUE_TYPE, TEST_ISSUE_PRIORITY, TEST_ISSUE_SUMMARY, TEST_ISSUE_DESCRIPTION,
                                stepsToReproduce, new ArrayList<>()
        );
        verify(issueRestClient, times(1)).createIssue(any());
    }

    @Test
    public void createIssueWithAttachments() throws URISyntaxException {
        final long bob = uc.createLab3AndBob();
        String issueDescription = "Reported by: " + getReporter(bob) + "\n\n" + TEST_ISSUE_DESCRIPTION;
        final String attachmentContent = "test txt content";
        final String attachmentFileName = "testFile.txt";
        final String attachmentFileType = "text/plain";


        final String base64Content = Base64.getEncoder().encodeToString(attachmentContent.getBytes());
        final IssueAttachment testAttachment =
            new IssueAttachment(attachmentFileName, base64Content, attachmentFileType, null);
        final ArrayList<IssueAttachment> attachments = newArrayList(testAttachment, testAttachment);

        Mockito.reset(jiraRestClientProvider);
        final IssueRestClient issueRestClient = verifyIssueDescription(jiraRestClientProvider, issueDescription);

        doAnswer(invocationOnMock -> {
            final AttachmentInput input1 = (AttachmentInput) invocationOnMock.getArguments()[1];
            final AttachmentInput input2 = (AttachmentInput) invocationOnMock.getArguments()[2];

            newArrayList(input1, input2).forEach(input -> {
                final String content = readDataFromInput(input);
                assertEquals(input.getFilename(), attachmentFileName, "Error. Wrong attachment file name");
                assertEquals(content, attachmentContent, "Error. Wrong attachment content were uploaded.");
            });
            return promise(null);
        }).when(issueRestClient).addAttachments(any(), (AttachmentInput) any(), any());

        jiraService.reportIssue(bob, TEST_ISSUE_TYPE, TEST_ISSUE_PRIORITY, TEST_ISSUE_SUMMARY, TEST_ISSUE_DESCRIPTION,
                                null, attachments
        );
        verify(issueRestClient, times(1)).addAttachments(any(), (AttachmentInput) any(), any());
        verify(issueRestClient, times(1)).createIssue(any());
    }

    @Test
    public void readAttachmentsThumbnail() throws URISyntaxException {
        final URI attachmentURI = new URI("http://localhost/attachment/1");
        final URI attachmentContentURI = new URI("http://localhost/attachment/1/content");
        final URI attachmentThumbnailURI = new URI("http://localhost/attachment/1/thumbnail");
        final String attachmentThumbnailContent = "test txt content";
        final String issuekey = "ALIS-123123";

        final byte[] thumbnailBytes = attachmentThumbnailContent.getBytes();
        final ByteArrayInputStream thumbnailInputStream = new ByteArrayInputStream(thumbnailBytes);
        final String thumbnailBase64 = Base64.getEncoder().encodeToString(thumbnailBytes);

        final Attachment attachment1 = new Attachment(attachmentURI, "attachment1", null, null,
                                                      1024, "text/plain", attachmentContentURI, null
        );
        final Attachment attachment2 = new Attachment(attachmentURI, "attachment2", null, null, 4424,
                                                      "image/png", attachmentContentURI, attachmentThumbnailURI
        );


        Mockito.reset(jiraRestClientProvider);

        final JiraRestClient restClient = mock(JiraRestClient.class);
        final IssueRestClient issueRestClient = mock(IssueRestClient.class);
        final Issue issue = mock(Issue.class);

        when(issue.getAttachments()).thenReturn(newArrayList(attachment1, attachment2));
        when(jiraRestClientProvider.get()).thenReturn(restClient);

        when(restClient.getIssueClient()).thenReturn(issueRestClient);
        when(issueRestClient.getIssue(eq(issuekey))).thenReturn(promise(issue));
        when(issueRestClient.getAttachment(eq(attachmentThumbnailURI))).thenReturn(promise(thumbnailInputStream));

        final List<IssueAttachment> issueAttachments = jiraService.getIssueAttachments(issuekey);
        final IssueAttachment issueAttachment1 = issueAttachments.get(0);
        final IssueAttachment issueAttachment2 = issueAttachments.get(1);

        assertEquals(issueAttachment1.name, attachment1.getFilename(), "Attachment named was readed wrong");
        assertEquals(issueAttachment1.type, attachment1.getMimeType(), "Attachment file type was readed wrong");
        assertNull(issueAttachment1.base64Content, "Attachment content shouldn't be read");
        assertNull(
            issueAttachment1.thumbnailBase64Content,
            "Attachment thumbnail for not image file shouldn't be read"
        );

        assertEquals(issueAttachment2.name, attachment2.getFilename(), "Attachment named was readed wrong");
        assertEquals(issueAttachment2.type, attachment2.getMimeType(), "Attachment file type was readed wrong");
        assertNull(issueAttachment2.base64Content, "Attachment content shouldn't be read");
        assertEquals(issueAttachment2.thumbnailBase64Content, thumbnailBase64, "Image thumbnail was read wrong.");
    }

    @Test
    public void sendComment() throws URISyntaxException {
        final long bob = uc.createLab3AndBob();
        final String issueKey = "TASK-1234";
        final String testCommentMessage = "Test comment message";
        final URI commentsURI = new URI("http://localhost/comments");
        final String commentBody = "Commented by: " + getReporter(bob) + "\n\n" + testCommentMessage;

        Mockito.reset(jiraRestClientProvider);

        final JiraRestClient restClient = mock(JiraRestClient.class);
        final IssueRestClient issueRestClient = mock(IssueRestClient.class);
        final Issue issue = mock(Issue.class);
        when(jiraRestClientProvider.get()).thenReturn(restClient);
        when(restClient.getIssueClient()).thenReturn(issueRestClient);
        when(issueRestClient.getIssue(eq(issueKey))).thenReturn(promise(issue));
        when(issue.getCommentsUri()).thenReturn(commentsURI);


        doAnswer(invocationOnMock -> {
            final Comment comment = (Comment) invocationOnMock.getArguments()[1];
            assertEquals(comment.getBody(), commentBody, "Error. Wrong comment was send.");
            return promise(null);
        }).when(issueRestClient).addComment(any(), any());

        jiraService.addComment(bob, issueKey, testCommentMessage);
        verify(issueRestClient, times(1)).addComment(any(), any());
    }

    private String readDataFromInput(AttachmentInput input) {
        final byte[] buffer = new byte[1024];
        try (final InputStream stream = input.getInputStream()) {
            final int read = stream.read(buffer);
            return new String(buffer, 0, read);
        } catch (IOException e) {
            final String errorMessage = "Can't read attachment content";
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    private String getReporter(long userId) {
        final PersonInfo personInfo = userReader.readPersonInfo(userId);
        return personInfo.firstName + " " + personInfo.lastName + " (" + personInfo.email + ")";
    }

    private IssueRestClient verifyIssueDescription(JiraRestClientProvider provider, String expectedDescription)
        throws URISyntaxException {
        final URI attachmentsURI = new URI("http://localhost/attachments");
        final URI statusURI = new URI("http://localhost/status/1");
        final Status issueStatus = new Status(statusURI, 1L, "TO DO", "TO DO", null);
        final BasicIssue resultedIssue = new BasicIssue(new URI(TEST_ISSUE_URL), TEST_ISSUE_KEY, TEST_ISSUE_ID);
        final JiraRestClient restClient = mock(JiraRestClient.class);
        final IssueRestClient issueRestClient = mock(IssueRestClient.class);
        final Issue issue = mock(Issue.class);
        when(issue.getStatus()).thenReturn(issueStatus);
        doAnswer(invocation -> promise(issue))
            .when(issueRestClient)
            .getIssue(eq(TEST_ISSUE_KEY));

        doAnswer(invocation -> attachmentsURI).when(issue).getAttachmentsUri();

        when(provider.get()).thenReturn(restClient);
        when(restClient.getIssueClient()).thenReturn(issueRestClient);

        doAnswer(invocationOnMock -> {
            final IssueInput input = (IssueInput) invocationOnMock.getArguments()[0];
            final IssueInputBuilder inputBuilder = new IssueInputBuilder(
                jiraPropertiesProvider.getJiraProjectKey(),
                TEST_ISSUE_TYPE
            )
                .setSummary(TEST_ISSUE_SUMMARY)
                .setDescription(expectedDescription)
                .setIssueTypeId(TEST_ISSUE_TYPE)
                .setPriorityId(TEST_ISSUE_PRIORITY);
            assertEquals(
                input.getFields(),
                inputBuilder.build().getFields(),
                "Error. Issue was created with wrong description."
            );

            return promise(resultedIssue);
        }).when(issueRestClient).createIssue(any());
        return issueRestClient;
    }

}
