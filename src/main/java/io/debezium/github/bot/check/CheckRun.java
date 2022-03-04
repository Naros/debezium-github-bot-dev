/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.github.bot.check;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import org.kohsuke.github.GHCheckRun;
import org.kohsuke.github.GHCheckRun.Conclusion;
import org.kohsuke.github.GHCheckRunBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the execution of a Bot check.
 *
 * @author Chris Cranford
 */
public class CheckRun {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckRun.class);

    private final CheckContext context;
    private final Check task;
    private final long id;

    CheckRun(CheckContext context, Check task, long id) {
        this.context = context;
        this.task = task;
        this.id = id;
    }

    /**
     * Run the task
     *
     * @return the output from the task, never null.
     * @throws IOException if an exception occurred
     */
    CheckRunOutput run() throws IOException {
        // Run the task
        CheckRunOutput output = new CheckRunOutput(id, task.name);
        task.run(context, output);

        // Resolve the conclusion based on the output's pass/failure flag
        Conclusion result = output.passed() ? Conclusion.SUCCESS : Conclusion.FAILURE;

        if (!context.deploymentConfig.isDryRun()) {
            // When not a dry-run, update GitHub
            context.repository.updateCheckRun(id)
                    .withCompletedAt(Date.from(Instant.now()))
                    .withStatus(GHCheckRun.Status.COMPLETED)
                    .withConclusion(result)
                    .add(new GHCheckRunBuilder.Output(output.title(), output.contents()))
                    .create();
        }
        else {
            // Simply log the output of the result to the logs when in dry-run mode.
            LOGGER.info("PR #{} - Update task run '{}' with result '{}'",
                    context.pullRequest.getNumber(), task.name, result);
        }

        return output;
    }

    /**
     * Creates a bot task execution.
     *
     * @param context the context of the task, never null
     * @param task the task to be performed, never null
     * @return the execution context of the task; never null
     * @throws IOException if an exception occurred creating the execution context
     */
    static CheckRun create(CheckContext context, Check task) throws IOException {
        if (!context.deploymentConfig.isDryRun()) {
            // When not a dry-run, update GitHub
            GHCheckRun checkRun = context.repository
                    .createCheckRun(task.name, context.pullRequest.getHead().getSha())
                    .withStartedAt(Date.from(Instant.now()))
                    .withStatus(GHCheckRun.Status.IN_PROGRESS)
                    .create();
            return new CheckRun(context, task, checkRun.getId());
        }
        else {
            // Simply log the output of the result to the logs when in dry-run mode.
            LOGGER.info("PR #{} - Creating task '{}'", context.pullRequest.getNumber(), task.name);
            return new CheckRun(context, task, 42L);
        }
    }
}
