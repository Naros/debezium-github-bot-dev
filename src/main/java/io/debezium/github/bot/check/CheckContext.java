/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.github.bot.check;

import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;

import io.debezium.github.bot.config.DeploymentConfig;

/**
 * Parameter object for passing information to checks.
 *
 * @author Chris Cranford
 */
public class CheckContext {

    public final DeploymentConfig deploymentConfig;
    public final GHRepository repository;
    public final GHPullRequest pullRequest;

    /**
     * Creates the context
     *
     * @param deploymentConfig the deployment configuration; must not be null
     * @param repository the github repository; must not be null
     * @param pullRequest the github pull request; must not be null
     */
    public CheckContext(DeploymentConfig deploymentConfig, GHRepository repository, GHPullRequest pullRequest) {
        this.deploymentConfig = deploymentConfig;
        this.repository = repository;
        this.pullRequest = pullRequest;
    }
}
