package com.infoclinika.mssharing.model.internal.write;

import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.PaginationItems.PagedItem;
import com.infoclinika.mssharing.model.PaginationItems.PagedItemInfo;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.Issue;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.repository.IssueRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.jira.JiraService;
import com.infoclinika.mssharing.model.jira.JiraService.BasicIssueProperty;
import com.infoclinika.mssharing.model.jira.JiraService.IssueAttachment;
import com.infoclinika.mssharing.model.jira.JiraService.JiraIssueDetails;
import com.infoclinika.mssharing.model.write.IssuesService;
import com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * @author Vladislav Kovchug
 */
@Service
@Transactional
public class IssuesServiceImpl implements IssuesService {
    private static final String DELETE_ISSUE_COMMENT_MESSAGE = "Issue deleted by user.";
    private static final int ISSUES_UPDATE_PAGE_SIZE = 100;

    private static final Logger LOGGER = LoggerFactory.getLogger(IssuesServiceImpl.class);

    private final IssueRepository issueRepository;
    private final JiraService jiraService;
    private final UserRepository userRepository;
    private final RuleValidator ruleValidator;
    private final Notifier notifier;

    @Inject
    public IssuesServiceImpl(
        IssueRepository issueRepository, JiraService jiraService, UserRepository userRepository,
        RuleValidator ruleValidator, Notifier notifier
    ) {
        this.issueRepository = issueRepository;
        this.jiraService = jiraService;
        this.userRepository = userRepository;
        this.ruleValidator = ruleValidator;
        this.notifier = notifier;
    }

    @Override
    public long reportIssue(
        long actor, IssueType issueType, String issuePriority, String summary, String description,
        String stepsToReproduce, List<IssueAttachment> attachments
    ) {
        LOGGER.info("Report issue.\nactor: " + actor +
            "\nissueType: " + issueType +
            "\nissuePriority: " + issuePriority +
            "\nsummary: " + summary +
            "\ndescription: " + description +
            "\nstepsToReproduce: " + stepsToReproduce +
            "\nattachments: " + attachments);

        final User owner = userRepository.findOne(actor);
        checkNotNull(owner, "Error. Can't find user with id: " + actor);

        final List<BasicIssueProperty> jiraIssueTypes = jiraService.getIssueTypes();
        final List<BasicIssueProperty> jiraPriorities = jiraService.getPriorities();

        final Optional<BasicIssueProperty> jiraIssueType = findProperty(jiraIssueTypes, issueType.name());
        checkState(jiraIssueType.isPresent(), "Can't find jira issueType id for Type: " + issueType +
            ". Jira issue types: " + jiraIssueTypes);

        final Optional<BasicIssueProperty> jiraIssuePriority = findProperty(jiraPriorities, issuePriority);
        checkState(jiraIssuePriority.isPresent(), "Can't find jira priority id with name: " + issuePriority +
            ". Jira priorities: " + jiraPriorities);

        final JiraIssueDetails issueDetails = jiraService.reportIssue(actor, jiraIssueType.get().id,
            jiraIssuePriority.get().id, summary, description, stepsToReproduce, attachments
        );

        final Issue issue =
            new Issue(summary, issueDetails.id, issueDetails.key, Issue.IssueType.valueOf(issueType.name()),
                owner, issueDetails.status, issuePriority, new Date(), description, stepsToReproduce
            );
        issueRepository.save(issue);

        return issue.getId();
    }

    @Override
    public long editIssue(
        long actor, long issueId, IssueType issueType, String issuePriority, String summary,
        String description, String stepsToReproduce, List<IssueAttachment> attachments
    ) {
        LOGGER.info("Edit issue.\nactor: " + actor +
            "\nissueId: " + issueId +
            "\nissueType: " + issueType +
            "\nissuePriority: " + issuePriority +
            "\nsummary: " + summary +
            "\ndescription: " + description +
            "\nstepsToReproduce: " + stepsToReproduce +
            "\nattachments: " + attachments);

        final Issue issue = getIssueIfAvailable(actor, issueId);

        final List<BasicIssueProperty> jiraIssueTypes = jiraService.getIssueTypes();
        final List<BasicIssueProperty> jiraPriorities = jiraService.getPriorities();

        final Optional<BasicIssueProperty> jiraIssueType = findProperty(jiraIssueTypes, issueType.name());
        checkState(jiraIssueType.isPresent(), "Can't find jira issueType id for Type: " + issueType +
            ". Jira issue types: " + jiraIssueTypes);

        final Optional<BasicIssueProperty> jiraIssuePriority = findProperty(jiraPriorities, issuePriority);
        checkState(jiraIssuePriority.isPresent(), "Can't find jira priority id with name: " + issuePriority +
            ". Jira priorities: " + jiraPriorities);

        jiraService.editIssue(actor, issue.getJiraKey(), jiraIssueType.get().id,
            jiraIssuePriority.get().id, summary, description, stepsToReproduce, attachments
        );

        issue.setName(summary);
        issue.setType(Issue.IssueType.valueOf(issueType.name()));
        issue.setPriority(issuePriority);
        issue.setDescription(description);
        issue.setStepsToReproduce(stepsToReproduce);

        issueRepository.save(issue);

        return issue.getId();
    }

    @Override
    public void deleteIssue(long actor, long issueId) {
        LOGGER.info("Delete issue. Issue id: {}. Actor: {}.", issueId, actor);

        final Issue issue = getIssueIfAvailable(actor, issueId);

        jiraService.addComment(actor, issue.getJiraKey(), DELETE_ISSUE_COMMENT_MESSAGE);
        issueRepository.delete(issue);
        if (!issue.getOwner().getId().equals(actor) && ruleValidator.hasAdminRights(actor)) {
            notifier.sendIssueDeletedByAdminNotification(issue.getOwner().getId(), issueId, issue.getName());
        }
    }

    @Override
    public PagedItem<IssueShortDetails> readUserIssues(long actor, PagedItemInfo pagedInfo) {
        LOGGER.info("read issues for user: {}. page request: {}", actor, pagedInfo);
        final PageRequest pageRequest = getPageRequest(pagedInfo);
        final Page<Issue> page = issueRepository.findByOwnerId(actor, pageRequest);
        return getPagedItem(page);
    }

    @Override
    public PagedItem<IssueShortDetails> readAllIssues(long actor, PagedItemInfo pagedInfo) {
        LOGGER.info("read all issues actor: {}. page request: {}", actor, pagedInfo);
        ValidatorPreconditions.checkAccess(
            ruleValidator.hasAdminRights(actor),
            "User has no permission to read all reported issues"
        );

        final PageRequest pageRequest = getPageRequest(pagedInfo);
        final Page<Issue> page = issueRepository.findAll(pageRequest);
        return getPagedItem(page);
    }

    @Override
    public IssueDetails readIssueDetails(long actor, long id) {
        LOGGER.info("Read issue details for issue: {}. User: {}", id, actor);

        final Issue issue = getIssueIfAvailable(actor, id);
        final List<IssueAttachment> issueAttachments = jiraService.getIssueAttachments(issue.getJiraKey());

        return new IssueDetails(issue.getId(),
            issue.getName(),
            issue.getType().name(),
            issue.getOwner().getFullName(),
            issue.getOwner().getId(),
            issue.getStatus(),
            issue.getPriority(),
            issue.getReportedDate(),
            issue.getDescription(),
            issue.getStepsToReproduce(),
            issueAttachments
        );
    }

    @Override
    public void updateIssuesStatus() {
        LOGGER.info("Start to update all jira issue statuses.");
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        boolean isLast;
        do {
            final PageRequest pageRequest = new PageRequest(atomicInteger.getAndIncrement(), ISSUES_UPDATE_PAGE_SIZE);
            final Page<Issue> page = issueRepository.findAll(pageRequest);
            updateIssuesStatus(page.getContent());
            isLast = page.isLast();
        } while (!isLast);
    }

    private void updateIssuesStatus(List<Issue> issues) {
        if (issues.isEmpty()) { // don't update empty issues list
            return;
        }

        final List<Long> ids = issues.stream()
            .map(Issue::getJiraId)
            .collect(Collectors.toList());
        final Map<Long, JiraIssueDetails> issuesDetailsToIdMap = jiraService.getIssuesDetails(ids);
        issues.forEach(issue -> {
            final JiraIssueDetails details = issuesDetailsToIdMap.get(issue.getJiraId());
            if (details != null) {
                issue.setStatus(details.status);
            } else {
                LOGGER.warn("Can't find issue details for issue with id: {} and key: {}",
                    issue.getJiraId(), issue.getJiraKey()
                );
            }
        });

        issueRepository.save(issues);
    }

    private Issue getIssueIfAvailable(long actor, long issueId) {
        final Issue issue = issueRepository.findOne(issueId);
        checkNotNull(issue, "Error. Can't find issue with id: ", issueId);

        ValidatorPreconditions.checkAccess(
            issue.getOwner().getId().equals(actor) ||
                ruleValidator.hasAdminRights(actor),
            "User has no permission to read issue. User id: " + actor + "Issue id: " + issueId
        );
        return issue;
    }

    private PageRequest getPageRequest(PagedItemInfo pagedInfo) {
        final Sort.Direction direction = pagedInfo.isSortingAsc ? ASC : DESC;
        return new PageRequest(pagedInfo.page, pagedInfo.items,
            direction, pagedInfo.sortingField
        );
    }

    private PagedItem<IssueShortDetails> getPagedItem(Page<Issue> page) {
        final List<IssueShortDetails> items = page.getContent()
            .stream()
            .map(issue -> {
                return new IssueShortDetails(issue.getId(), issue.getName(), issue.getType().name(),
                    issue.getOwner().getFullName(), issue.getOwner().getId(), issue.getStatus(),
                    issue.getPriority(), issue.getReportedDate(), issue.getJiraKey(), issue.getJiraId()
                );
            })
            .collect(Collectors.toList());

        return new PagedItem<>(page.getTotalPages(),
            page.getTotalElements(),
            page.getNumber(),
            page.getNumberOfElements(),
            items
        );
    }

    private Optional<BasicIssueProperty> findProperty(List<BasicIssueProperty> properties, String name) {
        return properties.stream()
            .filter(p -> p.alias.equalsIgnoreCase(name))
            .findFirst();
    }

}
