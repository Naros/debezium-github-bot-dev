/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.github.bot.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

/**
 * Bot configuration.
 *
 * @author Chris Cranford
 */
@ConfigMapping(prefix = "debezium-github-bot")
public interface DeploymentConfig {
    @WithName("dry-run")
    boolean isDryRun();

    @WithName("jira-url")
    String getJiraUrl();

    @WithName("user-name")
    String getUserName();

    @WithName("issue-key-pattern")
    String getIssueKeyPattern();
}
