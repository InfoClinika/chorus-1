package com.infoclinika.mssharing.model.internal.jira;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.input.AttachmentInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.jira.JiraService;
import com.infoclinika.mssharing.propertiesprovider.JiraPropertiesProvider;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Vladislav Kovchug
 */
@Service
@Transactional
public class JiraServiceImpl implements JiraService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JiraServiceImpl.class);

    private final UserRepository userRepository;
    private final JiraRestClientProvider jiraRestClientProvider;
    private final JiraPropertiesProvider jiraPropertiesProvider;

    @Inject
    public JiraServiceImpl(UserRepository userRepository,
                           JiraRestClientProvider jiraRestClientProvider,
                           JiraPropertiesProvider jiraPropertiesProvider) {
        this.userRepository = userRepository;
        this.jiraRestClientProvider = jiraRestClientProvider;
        this.jiraPropertiesProvider = jiraPropertiesProvider;
    }

    @Override
    public JiraIssueDetails reportIssue(
        long actor,
        Long issueType,
        Long issuePriority,
        String summary,
        String description,
        String stepsToReproduce,
        List<IssueAttachment> attachments
    ) {
        LOGGER.info("Create issue.\nactor: " + actor +
                        "\nissueType: " + issueType +
                        "\nissuePriority: " + issuePriority +
                        "\nsummary: " + summary +
                        "\ndescription: " + description +
                        "\nstepsToReproduce: " + stepsToReproduce +
                        "\nattachments: " + attachments);

        final IssueInput issueInput =
            fillIssueInput(actor, issueType, issuePriority, summary, description, stepsToReproduce);

        try (final JiraRestClient restClient = jiraRestClientProvider.get()) {
            final IssueRestClient issueClient = restClient.getIssueClient();
            final BasicIssue basicIssue = issueClient.createIssue(issueInput).claim();
            LOGGER.info("Issue created.\n" +
                            "Summary: " + summary + "\n" +
                            "Link: " + basicIssue.getSelf().toString());

            final Issue issue = issueClient.getIssue(basicIssue.getKey()).claim();

            attachFiles(issueClient, basicIssue.getKey(), issue.getAttachmentsUri(), attachments);

            return new JiraIssueDetails(basicIssue.getId(), basicIssue.getKey(), issue.getStatus().getName());
        } catch (Exception e) {
            final String errorMessage = "Can't create Jira issue";
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Override
    public JiraIssueDetails editIssue(
        long actor,
        String issueKey,
        Long issueType,
        Long issuePriority,
        String summary,
        String description,
        String stepsToReproduce,
        List<IssueAttachment> attachments
    ) {
        LOGGER.info("Update issue.\nactor: " + actor +
                        "\nissueKey: " + issueType +
                        "\nissueType: " + issueType +
                        "\nissuePriority: " + issuePriority +
                        "\nsummary: " + summary +
                        "\ndescription: " + description +
                        "\nstepsToReproduce: " + stepsToReproduce +
                        "\nattachments: " + attachments);

        final IssueInput issueInput =
            fillIssueInput(actor, issueType, issuePriority, summary, description, stepsToReproduce);

        try (final JiraRestClient restClient = jiraRestClientProvider.get()) {
            final IssueRestClient issueClient = restClient.getIssueClient();
            issueClient.updateIssue(issueKey, issueInput).claim();
            LOGGER.info("Issue updated.\n" +
                            "Summary: " + summary + "\n" +
                            "Issue key: " + issueKey);

            final Issue issue = issueClient.getIssue(issueKey).claim();

            attachFiles(issueClient, issueKey, issue.getAttachmentsUri(), attachments);
            return new JiraIssueDetails(issue.getId(), issue.getKey(), issue.getStatus().getName());
        } catch (Exception e) {
            final String errorMessage = "Can't update Jira issue";
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Override
    public void addComment(long actor, String issueKey, String message) {
        LOGGER.info("Add comment for issue: {}. Message: {}", issueKey, message);
        try (final JiraRestClient restClient = jiraRestClientProvider.get()) {
            final IssueRestClient issueClient = restClient.getIssueClient();
            final Issue issue = issueClient.getIssue(issueKey).claim();

            final String commentBody = "Commented by: " + getUserFullName(actor) + "\n\n" + message;

            final Comment comment = Comment.valueOf(commentBody);
            issueClient.addComment(issue.getCommentsUri(), comment).claim();
        } catch (IOException e) {
            final String errorMessage = "Can't add a comment to the issue.";
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Override
    public List<BasicIssueProperty> getPriorities() {
        LOGGER.debug("Read available issue priorities.");
        final ArrayList<BasicIssueProperty> result = new ArrayList<>();
        try (final JiraRestClient restClient = jiraRestClientProvider.get()) {
            final MetadataRestClient metadataClient = restClient.getMetadataClient();
            metadataClient.getPriorities()
                .claim()
                .forEach(priority -> {
                    result.add(toBasicIssueProperty(priority.getId(), priority.getName()));
                });

        } catch (IOException e) {
            final String errorMessage = "Can't read issue priorities.";
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }

        LOGGER.debug("Available issue priorities:" + result);
        return result;
    }

    @Override
    public List<BasicIssueProperty> getIssueTypes() {
        LOGGER.debug("Read available issue types.");
        final ArrayList<BasicIssueProperty> result = new ArrayList<>();
        try (final JiraRestClient restClient = jiraRestClientProvider.get()) {
            final MetadataRestClient metadataClient = restClient.getMetadataClient();
            metadataClient.getIssueTypes()
                .claim()
                .forEach(issueType -> {
                    result.add(toBasicIssueProperty(issueType.getId(), issueType.getName()));
                });

        } catch (IOException e) {
            final String errorMessage = "Can't read issue types.";
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }

        LOGGER.debug("Available issue types:" + result);
        return result;
    }

    @Override
    public Map<Long, JiraIssueDetails> getIssuesDetails(List<Long> ids) {
        LOGGER.info("Read issues details for ids: {}", ids);
        final HashMap<Long, JiraIssueDetails> result = new HashMap<>();
        if (ids.isEmpty()) {
            return result;
        }

        try (final JiraRestClient restClient = jiraRestClientProvider.get()) {
            final SearchRestClient searchClient = restClient.getSearchClient();

            final String joinedIds = StringUtils.join(ids, ",");
            final SearchResult searchResult = searchClient
                .searchJql("id in (" + joinedIds + ")", ids.size(), 0, null)
                .claim();
            searchResult.getIssues().forEach(issue -> {
                final JiraIssueDetails details = new JiraIssueDetails(issue.getId(), issue.getKey(),
                                                                      issue.getStatus().getName()
                );
                result.put(issue.getId(), details);
            });
        } catch (IOException e) {
            final String errorMessage = "Can't read issues details.";
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
        return result;
    }

    @Override
    public List<IssueAttachment> getIssueAttachments(String issueKey) {
        LOGGER.info("Read issue attachments for key: {}", issueKey);
        final ArrayList<IssueAttachment> result = new ArrayList<>();
        try (final JiraRestClient restClient = jiraRestClientProvider.get()) {
            final IssueRestClient issueClient = restClient.getIssueClient();
            final Issue issue = issueClient.getIssue(issueKey).claim();

            issue.getAttachments().forEach(attachment -> {
                String thumbnailBase64Content = null;
                if (attachment.hasThumbnail()) {
                    final InputStream thumbnail = issueClient.getAttachment(attachment.getThumbnailUri()).claim();
                    thumbnailBase64Content = convertToBase64String(thumbnail);
                }

                result.add(new IssueAttachment(attachment.getFilename(), null,
                                               attachment.getMimeType(), thumbnailBase64Content
                ));
            });
        } catch (IOException e) {
            final String errorMessage = "Can't read issue attachments.";
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
        return result;
    }

    private IssueInput fillIssueInput(long actor, Long issueType, Long issuePriority, String summary,
                                      String description, String stepsToReproduce) {
        final String reporterName = getUserFullName(actor);
        String composedDescription = "Reported by: " + reporterName + "\n\n" + description;
        if (!StringUtils.isEmpty(stepsToReproduce)) {
            composedDescription += "\n\nSteps to reproduce: \n" + stepsToReproduce + "\n\n";
        }

        LOGGER.info("Filling issue parameters." +
                        " User ID: " + actor +
                        " Summary: " + summary);

        final String jiraProjectKey = jiraPropertiesProvider.getJiraProjectKey();
        final IssueInputBuilder inputBuilder = new IssueInputBuilder(jiraProjectKey, issueType)
            .setSummary(summary)
            .setDescription(composedDescription)
            .setIssueTypeId(issueType)
            .setPriorityId(issuePriority);

        return inputBuilder.build();
    }

    private String getUserFullName(long actor) {
        final User reporter = userRepository.findOne(actor);
        return reporter.getFirstName() + " " + reporter.getLastName() + " (" + reporter.getEmail() + ")";
    }

    private String convertToBase64String(InputStream inputStream) {
        try {
            final byte[] bytes = IOUtils.toByteArray(inputStream);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            String errorMessage = "Error. Can't convert input stream to base64.";
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    private BasicIssueProperty toBasicIssueProperty(Long id, String name) {
        return new BasicIssueProperty(id, name, name.toLowerCase());
    }

    private void attachFiles(IssueRestClient issueClient,
                             String issueKey,
                             URI attachmentsUri,
                             List<IssueAttachment> attachments) {
        if (attachments.isEmpty()) {
            LOGGER.info("No attachments for issue " + issueKey);
            return;
        }

        final List<String> names = attachments.stream().map(attachment -> attachment.name).collect(Collectors.toList());
        LOGGER.info("Prepare attachments to upload: " + names);

        final AttachmentInput[] attachmentInputs = new AttachmentInput[attachments.size()];
        final AtomicInteger attachmentIndex = new AtomicInteger(0);
        attachments.forEach(attachment -> {
            checkNotNull(attachment.base64Content, "Error. no content for attachment", attachment);
            final byte[] bytes = Base64.getDecoder().decode(attachment.base64Content);
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            attachmentInputs[attachmentIndex.getAndIncrement()] = new AttachmentInput(attachment.name, inputStream);
        });

        LOGGER.info("Uploading attachments to issue " + issueKey);
        issueClient.addAttachments(attachmentsUri, attachmentInputs).claim();
        LOGGER.info("Issue attachments uploaded.");
    }
}
