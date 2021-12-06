/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.github.bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestCommitDetail;

import io.debezium.github.bot.check.Check;
import io.debezium.github.bot.check.CheckContext;
import io.debezium.github.bot.check.CheckRunOutput;
import io.debezium.github.bot.check.CheckRunRule;
import io.debezium.github.bot.config.DeploymentConfig;

/**
 * Applies Jira-specific rules to the pull request which include:
 *
 * <ul>
 *     <li>Notify user of issue keys in commits not included in pull request title</li>
 *     <li>Notify user of all issue keys and links to those issues present in the pull request</li>
 * </ul>
 *
 * @author Chris Cranford
 */
public class JiraIssueCheck extends Check {

    private final Pattern issueKeyPattern;
    private final DeploymentConfig deploymentConfig;

    JiraIssueCheck(DeploymentConfig deploymentConfig) {
        super("JIRA");
        this.deploymentConfig = deploymentConfig;
        this.issueKeyPattern = Pattern.compile(deploymentConfig.getIssueKeyPattern(), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    }

    @Override
    public void run(CheckContext context, CheckRunOutput output) throws IOException {
        // Examine commit history to gather issue key state
        Set<String> issueKeys = new LinkedHashSet<>();
        Set<String> commitsWithMessageNotStartingWithIssueKey = new LinkedHashSet<>();
        for (GHPullRequestCommitDetail commitDetails : context.pullRequest.listCommits()) {
            GHPullRequestCommitDetail.Commit commit = commitDetails.getCommit();
            String message = commit.getMessage();
            Matcher commitMessageIssueKeyMatcher = issueKeyPattern.matcher(message);
            int issueKeyIndex = commitMessageIssueKeyMatcher.find() ? commitMessageIssueKeyMatcher.start() : -1;
            if (issueKeyIndex == 0) {
                issueKeys.add(commitMessageIssueKeyMatcher.group());
            }
            else {
                commitsWithMessageNotStartingWithIssueKey.add(commitDetails.getSha());
            }
        }

        // Perform checks
        issuesAddressed(output, issueKeys);
        // commitStartWithKey(output, commitsWithMessageNotStartingWithIssueKey);
        issuesNotMentioned(output, getIssueKeysNotMentionedInTitleBody(context.pullRequest, issueKeys));
    }

    private List<String> getIssueKeysNotMentionedInTitleBody(GHPullRequest pullRequest, Set<String> issueKeys) {
        final String title = pullRequest.getTitle();
        final String body = pullRequest.getBody();
        return issueKeys.stream().filter(k -> {
            if (title != null && !title.contains(k)) {
                return true;
            }
            return body != null && !body.contains(k);
        }).collect(Collectors.toList());
    }

    private void issuesNotMentioned(CheckRunOutput output, List<String> issuesNotMentioned) {
        CheckRunRule pullRequestRule = output.rule("All issues addressed should be included in PR title");
        if (issuesNotMentioned.isEmpty()) {
            pullRequestRule.passed();
        }
        else {
            List<String> responses = new ArrayList<>();
            for (String issueKey : issuesNotMentioned) {
                responses.add("Issue not mentioned: " + getJiraUrlShortAndLongLink(issueKey));
            }
            pullRequestRule.failed(responses);
        }
    }

    private void commitStartWithKey(CheckRunOutput output, Set<String> commitsWithoutIssueKeys) {
        // todo: disabled for now as a GH action handles this but kept this as an example
        CheckRunRule commitRule = output.rule("All commit messages should start with DBZ-XXXX issue key");
        if (commitsWithoutIssueKeys.isEmpty()) {
            commitRule.passed();
        }
        else {
            List<String> responses = new ArrayList<>();
            for (String commit : commitsWithoutIssueKeys) {
                responses.add("Offending commit " + commit);
            }
            commitRule.failed(responses);
        }
    }

    private void issuesAddressed(CheckRunOutput output, Set<String> issueKeys) {
        CheckRunRule issuesAddressed = output.rule("Jira issues addressed in this pull request");
        issuesAddressed.icon(":large_blue_diamond:");
        List<String> responses = new ArrayList<>();
        if (issueKeys.isEmpty()) {
            // Mark as passed but don't force inclusion when only showing failures
            issuesAddressed.passed();
        }
        else {
            for (String issueKey : issueKeys) {
                responses.add(getJiraUrlShortAndLongLink(issueKey));
            }
            issuesAddressed.passed(responses);
            issuesAddressed.always();
        }
    }

    private String getLink(String text, String url) {
        return "[" + text + "](" + url + ")";
    }

    private String getJiraUrl(String issueKey) {
        return deploymentConfig.getJiraUrl() + issueKey;
    }

    private String getJiraUrlShortAndLongLink(String issueKey) {
        return getLink(issueKey, getJiraUrl(issueKey)) + " - " + getJiraUrl(issueKey);
    }
}
