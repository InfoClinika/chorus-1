package com.infoclinika.mssharing.model.test.jira;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.AttachmentInput;
import com.google.common.base.Optional;
import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.PaginationItems.PagedItem;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.internal.jira.JiraRestClientProvider;
import com.infoclinika.mssharing.model.jira.JiraService.IssueAttachment;
import com.infoclinika.mssharing.model.write.IssuesService;
import com.infoclinika.mssharing.model.write.IssuesService.IssueDetails;
import com.infoclinika.mssharing.model.write.IssuesService.IssueShortDetails;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.propertiesprovider.JiraPropertiesProvider;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.atlassian.util.concurrent.Promises.promise;
import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.model.write.IssuesService.IssueType.BUG;
import static com.infoclinika.mssharing.model.write.IssuesService.IssueType.IMPROVEMENT;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * @author Vladislav Kovchug
 */
public class IssueServiceTest extends AbstractTest {

    private static final String TEST_ISSUE_SUMMARY = "test summary";
    private static final String TEST_ISSUE_DESCRIPTION = "test issue description";
    private static final String TO_DO_STATUS = "To Do";
    private static final String MINOR_PRIORITY = "minor";
    private static final String HIGH_PRIORITY = "high";
    private static final String TEST_ISSUE_URL = "http://jira.com/issue/CP-1234";
    private static final List<String> JIRA_PRIORITIES_NAMES =
        newArrayList(
            "Blocker",
            "Critical",
            "Highest",
            "Minor",
            "Trivial",
            "Medium",
            "High"
        );
    private static final List<String> JIRA_ISSUE_TYPES_NAMES = newArrayList(
        "Sub-task",
        "Task",
        "Sub-Task Bug",
        "Bug",
        "Technical task",
        "Epic",
        "Improvement",
        "Change",
        "Story",
        "New Feature"
    );

    @Inject
    private IssuesService issuesService;

    @Inject
    private JiraRestClientProvider jiraRestClientProvider;

    @Inject
    private JiraPropertiesProvider jiraPropertiesProvider;

    @Test
    public void persistReportedIssueToDatabase() {
        final long bob = uc.createLab3AndBob();

        Mockito.reset(jiraRestClientProvider);
        mockJiraRestClient(jiraRestClientProvider);

        issuesService.reportIssue(
            bob,
            BUG,
            MINOR_PRIORITY,
            TEST_ISSUE_SUMMARY,
            TEST_ISSUE_DESCRIPTION,
            null,
            new ArrayList<>()
        );

        final PagedItem<IssueShortDetails> issues = issuesService.readUserIssues(bob, getPagedItemInfo());
        final IssueShortDetails details = issues.items.get(0);
        assertEquals(details.name, TEST_ISSUE_SUMMARY, "Created issue name doesn't match.");
        assertEquals(details.status, TO_DO_STATUS, "Created issue status doesn't match.");
        assertEquals(details.priority, MINOR_PRIORITY, "Created issue priority doesn't match.");
        assertEquals(details.issueType, BUG.name(), "Created issue type doesn't match.");
        assertEquals(details.ownerId, bob, "Created issue owner doesn't match.");
    }

    @Test
    public void userCanReadOnlyUsersIssues() {
        final long bob = uc.createLab3AndBob();
        final long kate = uc.createKateAndLab2();

        Mockito.reset(jiraRestClientProvider);
        mockJiraRestClient(jiraRestClientProvider);

        issuesService.reportIssue(
            bob,
            BUG,
            MINOR_PRIORITY,
            TEST_ISSUE_SUMMARY,
            TEST_ISSUE_DESCRIPTION,
            null,
            new ArrayList<>()
        );
        issuesService.reportIssue(
            kate,
            BUG,
            MINOR_PRIORITY,
            TEST_ISSUE_SUMMARY,
            TEST_ISSUE_DESCRIPTION,
            null,
            new ArrayList<>()
        );
        issuesService.reportIssue(
            kate,
            BUG,
            MINOR_PRIORITY,
            TEST_ISSUE_SUMMARY,
            TEST_ISSUE_DESCRIPTION,
            null,
            new ArrayList<>()
        );

        final PagedItem<IssueShortDetails> issues = issuesService.readUserIssues(kate, getPagedItemInfo());
        assertEquals(issues.items.size(), 2, "User readed wrong number of issues");
    }

    @Test
    public void adminCanReadAllIssues() {
        final long bob = uc.createLab3AndBob();
        final long kate = uc.createKateAndLab2();

        Mockito.reset(jiraRestClientProvider);
        mockJiraRestClient(jiraRestClientProvider);

        issuesService.reportIssue(
            bob,
            BUG,
            MINOR_PRIORITY,
            TEST_ISSUE_SUMMARY,
            TEST_ISSUE_DESCRIPTION,
            null,
            new ArrayList<>()
        );
        issuesService.reportIssue(
            kate,
            BUG,
            MINOR_PRIORITY,
            TEST_ISSUE_SUMMARY,
            TEST_ISSUE_DESCRIPTION,
            null,
            new ArrayList<>()
        );
        issuesService.reportIssue(
            kate,
            BUG,
            MINOR_PRIORITY,
            TEST_ISSUE_SUMMARY,
            TEST_ISSUE_DESCRIPTION,
            null,
            new ArrayList<>()
        );

        final PagedItem<IssueShortDetails> issues = issuesService.readAllIssues(admin(), getPagedItemInfo());
        assertEquals(issues.items.size(), 3, "Admin readed not all issues");
    }

    @Test
    public void testUpdateIssuesStatuses() {
        final long bob = uc.createLab3AndBob();
        final String updatedStatus = "New updated status";

        Mockito.reset(jiraRestClientProvider);
        mockJiraRestClient(jiraRestClientProvider);

        issuesService.reportIssue(
            bob,
            BUG,
            MINOR_PRIORITY,
            TEST_ISSUE_SUMMARY,
            TEST_ISSUE_DESCRIPTION,
            null,
            new ArrayList<>()
        );

        final PagedItem<IssueShortDetails> issues = issuesService.readUserIssues(bob, getPagedItemInfo());
        final IssueShortDetails details = issues.items.get(0);
        assertEquals(details.status, TO_DO_STATUS, "Created issue status doesn't match.");

        final JiraRestClient restClient = jiraRestClientProvider.get();
        final SearchRestClient searchRestClient = mock(SearchRestClient.class);
        when(restClient.getSearchClient()).thenReturn(searchRestClient);
        final ArrayList<Issue> searchResultsItems =
            newArrayList(getIssueWithStatus(details.jiraKey, details.jiraId, updatedStatus));
        final SearchResult searchResult = new SearchResult(0, searchResultsItems.size(),
            searchResultsItems.size(), searchResultsItems
        );
        when(searchRestClient.searchJql(any(), any(), any(), any())).thenReturn(promise(searchResult));

        issuesService.updateIssuesStatus();

        final PagedItem<IssueShortDetails> updatedIssues = issuesService.readUserIssues(bob, getPagedItemInfo());
        final IssueShortDetails updatedDetails = updatedIssues.items.get(0);

        assertEquals(updatedDetails.status, updatedStatus, "Issue status wasn't updated.");
    }

    @Test
    public void updateIssue() {
        final long bob = uc.createLab3AndBob();

        Mockito.reset(jiraRestClientProvider);
        mockJiraRestClient(jiraRestClientProvider);

        issuesService.reportIssue(
            bob,
            BUG,
            MINOR_PRIORITY,
            TEST_ISSUE_SUMMARY,
            TEST_ISSUE_DESCRIPTION,
            null,
            new ArrayList<>()
        );

        final PagedItem<IssueShortDetails> issues = issuesService.readUserIssues(bob, getPagedItemInfo());
        final IssueShortDetails details = issues.items.get(0);

        final String editedSummary = "edited summary";
        issuesService.editIssue(bob, details.id, IMPROVEMENT, HIGH_PRIORITY, editedSummary, TEST_ISSUE_DESCRIPTION,
            null, new ArrayList<>()
        );

        final PagedItem<IssueShortDetails> editedIssues = issuesService.readUserIssues(bob, getPagedItemInfo());
        final IssueShortDetails editedIssue = editedIssues.items.get(0);

        assertEquals(editedIssue.name, editedSummary, "Updated issue name doesn't match.");
        assertEquals(editedIssue.status, TO_DO_STATUS, "Updated issue status doesn't match.");
        assertEquals(editedIssue.priority, HIGH_PRIORITY, "Updated issue priority doesn't match.");
        assertEquals(editedIssue.issueType, IMPROVEMENT.name(), "Updated issue type doesn't match.");
        assertEquals(editedIssue.ownerId, bob, "Updated issue owner doesn't match.");
    }

    @Test
    public void updateIssueDoesntDeleteAttachments() {
        final long bob = uc.createLab3AndBob();
        final String attachmentContent = "test txt content";
        final String attachmentFileName = "testFile.txt";
        final String attachmentFileType = "text/plain";

        Mockito.reset(jiraRestClientProvider);
        mockJiraRestClient(jiraRestClientProvider);
        final IssueRestClient issueClient = jiraRestClientProvider.get().getIssueClient();
        when(issueClient.addAttachments(any(), (AttachmentInput) any(), any(), any())).thenReturn(promise(null));

        final String base64Content = Base64.getEncoder().encodeToString(attachmentContent.getBytes());
        final IssueAttachment testAttachment =
            new IssueAttachment(
                attachmentFileName,
                base64Content,
                attachmentFileType,
                null
            );
        final ArrayList<IssueAttachment> attachments = newArrayList(testAttachment, testAttachment, testAttachment);

        final long issueId =
            issuesService.reportIssue(bob, BUG, MINOR_PRIORITY, TEST_ISSUE_SUMMARY, TEST_ISSUE_DESCRIPTION,
                null, attachments
            );

        final IssueDetails details = issuesService.readIssueDetails(bob, issueId);

        issuesService.editIssue(bob, details.id, IMPROVEMENT, HIGH_PRIORITY, TEST_ISSUE_SUMMARY, TEST_ISSUE_DESCRIPTION,
            null, new ArrayList<>()
        );
        verify(issueClient, never()).addAttachments(any(), (AttachmentInput) any());
    }


    @Test
    public void updateIssueOnlyAddsNewAttachments() {
        final long bob = uc.createLab3AndBob();
        final String attachmentContent = "test txt content";
        final String updatedAttachmentContent = "updated attachment content";
        final String attachmentFileName = "testFile.txt";
        final String newAttachmentFileName = "newTestFile.txt";
        final String attachmentFileType = "text/plain";

        Mockito.reset(jiraRestClientProvider);
        mockJiraRestClient(jiraRestClientProvider);
        final IssueRestClient issueClient = jiraRestClientProvider.get().getIssueClient();
        when(issueClient.addAttachments(any(), (AttachmentInput) any(), any(), any())).thenReturn(promise(null));

        final String base64Content = Base64.getEncoder().encodeToString(attachmentContent.getBytes());
        final String updatedBase64Content = Base64.getEncoder().encodeToString(updatedAttachmentContent.getBytes());
        final IssueAttachment testAttachment =
            new IssueAttachment(attachmentFileName, base64Content, attachmentFileType,
                null
            );
        final IssueAttachment newAttachment =
            new IssueAttachment(newAttachmentFileName, updatedBase64Content, attachmentFileType,
                null
            );
        final ArrayList<IssueAttachment> attachments = newArrayList(testAttachment, testAttachment, testAttachment);
        final ArrayList<IssueAttachment> newAttachments = newArrayList(newAttachment);

        final long issueId =
            issuesService.reportIssue(bob, BUG, MINOR_PRIORITY, TEST_ISSUE_SUMMARY, TEST_ISSUE_DESCRIPTION,
                null, attachments
            );

        final IssueDetails details = issuesService.readIssueDetails(bob, issueId);

        doAnswer(invocationOnMock -> {
            final AttachmentInput input = (AttachmentInput) invocationOnMock.getArguments()[1];
            final String content = readDataFromInput(input);
            assertEquals(input.getFilename(), newAttachmentFileName);
            assertEquals(content, updatedAttachmentContent);

            return promise(null);
        }).when(issueClient).addAttachments(any(), (AttachmentInput) any());
        ;

        issuesService.editIssue(
            bob,
            details.id,
            IMPROVEMENT,
            HIGH_PRIORITY,
            TEST_ISSUE_SUMMARY,
            TEST_ISSUE_DESCRIPTION,
            null,
            newAttachments
        );
        verify(issueClient, never()).addAttachments(any(), (AttachmentInput) any(), any());
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void onlyOwnerCanEditIssues() {
        final long bob = uc.createLab3AndBob();
        final long kate = uc.createKateAndLab2();

        Mockito.reset(jiraRestClientProvider);
        mockJiraRestClient(jiraRestClientProvider);

        final long issueId =
            issuesService.reportIssue(bob, BUG, MINOR_PRIORITY, TEST_ISSUE_SUMMARY, TEST_ISSUE_DESCRIPTION,
                null, new ArrayList<>()
            );

        issuesService.editIssue(
            kate,
            issueId,
            BUG,
            MINOR_PRIORITY,
            TEST_ISSUE_SUMMARY,
            TEST_ISSUE_DESCRIPTION,
            null,
            new ArrayList<>()
        );
    }

    @Test
    public void testAdminCanEditAllIssues() {
        final long bob = uc.createLab3AndBob();
        final String newIssueName = "Edited by admin";

        Mockito.reset(jiraRestClientProvider);
        mockJiraRestClient(jiraRestClientProvider);

        final long issueId =
            issuesService.reportIssue(bob, BUG, MINOR_PRIORITY, TEST_ISSUE_SUMMARY, TEST_ISSUE_DESCRIPTION,
                null, new ArrayList<>()
            );

        issuesService.editIssue(admin(), issueId, BUG, MINOR_PRIORITY, newIssueName, TEST_ISSUE_DESCRIPTION,
            null, new ArrayList<>()
        );

        final IssueDetails issueDetails = issuesService.readIssueDetails(bob, issueId);
        assertEquals(issueDetails.name, newIssueName, "Error. Issue wan't edited.");
    }

    @Test
    public void issueDeletion() {
        final long bob = uc.createLab3AndBob();

        Mockito.reset(jiraRestClientProvider);
        mockJiraRestClient(jiraRestClientProvider);

        final long issueId =
            issuesService.reportIssue(bob, BUG, MINOR_PRIORITY, TEST_ISSUE_SUMMARY, TEST_ISSUE_DESCRIPTION,
                null, new ArrayList<>()
            );

        final PagedItem<IssueShortDetails> details = issuesService.readUserIssues(bob, getPagedItemInfo());
        assertEquals(details.itemsCount, 1L, "Error. Issue wasn't created.");

        issuesService.deleteIssue(bob, issueId);

        final PagedItem<IssueShortDetails> emptyDetails = issuesService.readUserIssues(bob, getPagedItemInfo());
        assertEquals(emptyDetails.itemsCount, 0L, "Error. Issue wasn't deleted.");
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void onlyOwnerCanDeleteIssues() {
        final long bob = uc.createLab3AndBob();
        final long kate = uc.createKateAndLab2();

        Mockito.reset(jiraRestClientProvider);
        mockJiraRestClient(jiraRestClientProvider);

        final long issueId =
            issuesService.reportIssue(bob, BUG, MINOR_PRIORITY, TEST_ISSUE_SUMMARY, TEST_ISSUE_DESCRIPTION,
                null, new ArrayList<>()
            );

        issuesService.deleteIssue(kate, issueId);
    }

    @Test
    public void adminCanDeleteAnyIssue() {
        final long bob = uc.createLab3AndBob();

        Mockito.reset(jiraRestClientProvider);
        mockJiraRestClient(jiraRestClientProvider);

        final long issueId =
            issuesService.reportIssue(bob, BUG, MINOR_PRIORITY, TEST_ISSUE_SUMMARY, TEST_ISSUE_DESCRIPTION,
                null, new ArrayList<>()
            );

        final PagedItem<IssueShortDetails> details = issuesService.readUserIssues(bob, getPagedItemInfo());
        assertEquals(details.itemsCount, 1L, "Error. Issue wasn't created.");

        issuesService.deleteIssue(admin(), issueId);
        final PagedItem<IssueShortDetails> emptyDetails = issuesService.readUserIssues(admin(), getPagedItemInfo());
        assertEquals(emptyDetails.itemsCount, 0L, "Error. Issue wasn't deleted.");
    }

    @Test
    public void issueDeletionSendsComment() {
        final long bob = uc.createLab3AndBob();
        final String newIssueName = "Edited by admin";

        Mockito.reset(jiraRestClientProvider);
        mockJiraRestClient(jiraRestClientProvider);
        final JiraRestClient restClient = jiraRestClientProvider.get();
        final IssueRestClient issueClient = restClient.getIssueClient();

        final long issueId =
            issuesService.reportIssue(
                bob,
                BUG,
                MINOR_PRIORITY,
                TEST_ISSUE_SUMMARY,
                TEST_ISSUE_DESCRIPTION,
                null,
                new ArrayList<>()
            );

        issuesService.deleteIssue(bob, issueId);

        verify(issueClient, times(1)).addComment(any(), any());
    }

    @Test
    public void issueDeletionByAdminSendsEmailToUser() {
        final long bob = uc.createLab3AndBob();

        final Notifier notificator = notificator();
        Mockito.reset(jiraRestClientProvider);
        Mockito.reset(notificator);
        mockJiraRestClient(jiraRestClientProvider);

        final long issueId =
            issuesService.reportIssue(bob, BUG, MINOR_PRIORITY, TEST_ISSUE_SUMMARY, TEST_ISSUE_DESCRIPTION,
                null, new ArrayList<>()
            );

        issuesService.deleteIssue(admin(), issueId);

        verify(notificator, times(1))
            .sendIssueDeletedByAdminNotification(eq(bob), eq(issueId), eq(TEST_ISSUE_SUMMARY));
    }

    @Test
    public void issueDeletionByUserDoesntSendEmail() {
        final Notifier notificator = notificator();
        Mockito.reset(jiraRestClientProvider);
        Mockito.reset(notificator);
        mockJiraRestClient(jiraRestClientProvider);

        final long issueId =
            issuesService.reportIssue(admin(), BUG, MINOR_PRIORITY, TEST_ISSUE_SUMMARY, TEST_ISSUE_DESCRIPTION,
                null, new ArrayList<>()
            );

        issuesService.deleteIssue(admin(), issueId);

        verify(notificator, times(0))
            .sendIssueDeletedByAdminNotification(anyLong(), anyLong(), any());
    }

    private PaginationItems.PagedItemInfo getPagedItemInfo() {
        return new PaginationItems.PagedItemInfo(
            100,
            0,
            "id",
            false,
            null,
            Optional.absent()
        );
    }

    private void mockJiraRestClient(JiraRestClientProvider provider) {
        final BasicIssue resultedIssue = getRandomBasicIssue();
        final JiraRestClient restClient = mock(JiraRestClient.class);
        final IssueRestClient issueRestClient = mock(IssueRestClient.class);
        final MetadataRestClient metadataRestClient = mock(MetadataRestClient.class);

        when(provider.get()).thenReturn(restClient);
        when(restClient.getIssueClient()).thenReturn(issueRestClient);
        when(restClient.getMetadataClient()).thenReturn(metadataRestClient);
        when(metadataRestClient.getIssueTypes()).thenReturn(promise(getJiraIssueTypes(JIRA_ISSUE_TYPES_NAMES)));
        when(metadataRestClient.getPriorities()).thenReturn(promise(getJiraPriorities(JIRA_PRIORITIES_NAMES)));
        when(issueRestClient.updateIssue(any(), any())).thenReturn(promise(null));
        when(issueRestClient.addComment(any(), any())).thenReturn(promise(null));

        when(issueRestClient.getIssue(any()))
            .thenReturn(promise(getIssueWithStatus(resultedIssue.getKey(), resultedIssue.getId(), TO_DO_STATUS)));

        when(issueRestClient.createIssue(any())).thenReturn(promise(resultedIssue));
    }

    private BasicIssue getRandomBasicIssue() {
        final Random random = new Random();
        final long issueId = random.nextInt(10000);
        final long issueKeyId = random.nextInt(10000);
        return new BasicIssue(getURI(TEST_ISSUE_URL), jiraPropertiesProvider.getJiraProjectKey() + issueKeyId, issueId);
    }

    private Issue getIssueWithStatus(String key, Long id, String status) {
        final URI issueURL = getURI(TEST_ISSUE_URL);
        final Status issueStatus = new Status(null, 1L, status, status, null);
        return new Issue(null, issueURL, key, id, null, null, issueStatus,
            null, null, null, newArrayList(), null, null,
            null, null, null, null, null, null,
            null, null, null, null, null, null,
            null, null, null, null, null, null,
            null
        );
    }

    private List<IssueType> getJiraIssueTypes(List<String> names) {
        final AtomicLong atomicLong = new AtomicLong(0);
        return names.stream()
            .map(name -> {
                return new IssueType(null, atomicLong.incrementAndGet(),
                    name, false, name, null
                );
            })
            .collect(Collectors.toList());
    }

    private List<Priority> getJiraPriorities(List<String> names) {
        final AtomicLong atomicLong = new AtomicLong(0);
        return names.stream()
            .map(name -> {
                return new Priority(null, atomicLong.incrementAndGet(),
                    name, "", name, null
                );
            })
            .collect(Collectors.toList());
    }

    private String readDataFromInput(AttachmentInput input) {
        final byte[] buffer = new byte[1024];
        try (final InputStream stream = input.getInputStream()) {
            final int read = stream.read(buffer);
            return new String(buffer, 0, read);
        } catch (IOException e) {
            final String errorMessage = "Can't read attachment content";
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    private URI getURI(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            log.error("Can't parse URL: " + url);
        }

        return null;
    }

}
