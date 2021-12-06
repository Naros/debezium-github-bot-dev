/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.github.bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.kohsuke.github.GHEventPayload.PullRequest;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.github.bot.check.Check;
import io.debezium.github.bot.check.CheckContext;
import io.debezium.github.bot.check.CheckRunOutput;
import io.debezium.github.bot.config.DeploymentConfig;
import io.quarkiverse.githubapp.event.PullRequest.Edited;
import io.quarkiverse.githubapp.event.PullRequest.Opened;
import io.quarkiverse.githubapp.event.PullRequest.Reopened;
import io.quarkiverse.githubapp.event.PullRequest.Synchronize;

/**
 * Handler that responds to pull request events.
 *
 * @author Chris Cranford
 */
public class PullRequestContributionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PullRequestContributionHandler.class);

    private static final String COMMENT_INTRO_PASSED = "Thanks for your pull request!\n\n"
            + "This pull request appears to follow the contribution rules. :+1:\n";
    private static final String COMMENT_INTRO_FAILED = "Thanks for your pull request!\n\n"
            + "This pull request does not follow the contribution rules. Could you please review?\n";
    private static final String COMMENT_FOOTER = "\n\n---\n\n:robot: This is an auto-generated message.";

    @Inject
    DeploymentConfig deploymentConfig;

    /**
     * Handler invoked when a pull request is opened, reopened, edited, or synchronized.
     *
     * @param payload the pull request; never null
     * @throws IOException if an error occurred
     */
    void pullRequestEvent(@Opened @Reopened @Edited @Synchronize PullRequest payload) throws IOException {
        GHRepository repository = payload.getRepository();
        GHPullRequest pullRequest = payload.getPullRequest();

        LOGGER.info("Event received for pull request {}.", pullRequest.getNumber());

        if (!shouldCheck(repository, pullRequest)) {
            return;
        }

        // Create checks and generate their output
        List<CheckRunOutput> outputs = createChecksAndRun(new CheckContext(deploymentConfig, repository, pullRequest));

        // Verify if the checks all passed
        boolean passed = outputs.stream().allMatch(CheckRunOutput::passed);

        // Avoid creating noisy comments if the issue is closed.
        if (GHIssueState.CLOSED.equals(pullRequest.getState())) {
            return;
        }

        // Generate the comment text
        StringBuilder message = new StringBuilder(passed ? COMMENT_INTRO_PASSED : COMMENT_INTRO_FAILED);
        outputs.forEach(output -> output.appendFailingRules(message));
        message.append(COMMENT_FOOTER);

        // Find the comment that should be modified by the bot; may be null if none exist.
        GHIssueComment existingComment = findExistingComment(pullRequest);
        if (!deploymentConfig.isDryRun()) {
            if (existingComment == null) {
                // No comment found, add a new comment with the message
                pullRequest.comment(message.toString());
            }
            else {
                // Existing comment detected, update the contents
                existingComment.update(message.toString());
            }
        }
        else {
            // In dry-mode run; while contents to the log instead
            LOGGER.info("PR #{} - Added comment {}", pullRequest.getNumber(), message.toString());
        }
    }

    /**
     * Check whether the pull request contribution checks should fire.
     *
     * GitHub sometimes mentions pull requests in the payload that are definitely not related to the changes, such as
     * very old pull requests on the branch that just got updated or pull requests on different repositories. We have to
     * ignore these to avoid creating comments on old pull requests.
     *
     * @param repository the repository
     * @param pullRequest the pull request
     * @return true if the checks should be executed; false otherwise.
     */
    private boolean shouldCheck(GHRepository repository, GHPullRequest pullRequest) {
        return !GHIssueState.CLOSED.equals(pullRequest.getState())
                && repository.getId() == pullRequest.getBase().getRepository().getId();
    }

    /**
     * Create all checks and run them.
     *
     * @param context the context; never null
     * @return the list of outputs per check, never null
     * @throws IOException if an error occurred
     */
    private List<CheckRunOutput> createChecksAndRun(CheckContext context) throws IOException {
        List<Check> checks = createChecks();

        List<CheckRunOutput> outputs = new ArrayList<>();
        for (Check check : checks) {
            outputs.add(Check.run(context, check));
        }

        return outputs;
    }

    /**
     * Create all checks related to pull requests.
     *
     * @return a list of checks that to be executed; never null
     */
    private List<Check> createChecks() {
        List<Check> checks = new ArrayList<>();
        checks.add(new JiraIssueCheck(deploymentConfig));
        checks.add(new PullRequestTitleCheck());
        return checks;
    }

    /**
     * Finds the existing comment added by the bot; if one exists.
     *
     * @param pullRequest the pull request to examine; must not be null
     * @return the comment or null if none were detected
     * @throws IOException if there was a problem reading the GitHub pull request data
     */
    private GHIssueComment findExistingComment(GHPullRequest pullRequest) throws IOException {
        for (GHIssueComment comment : pullRequest.listComments()) {
            if (comment.getUser().getLogin().contains(deploymentConfig.getUserName())) {
                return comment;
            }
        }
        return null;
    }
}
