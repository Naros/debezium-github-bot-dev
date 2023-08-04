/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.github.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.github.bot.config.DeploymentConfig;
import io.quarkus.runtime.StartupEvent;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * The entrypoint to the Debezium GitHub Bot application.
 *
 * @author Chris Cranford
 */
public class BotApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotApplication.class);

    @Inject
    DeploymentConfig deploymentConfig;

    void init(@Observes StartupEvent startupEvent) {
        LOGGER.info("Debezium GitHub Bot started.");
        if (deploymentConfig.isDryRun()) {
            LOGGER.warn("** Debezium GitHub Bot running in dry-run mode! **");
        }
    }
}
