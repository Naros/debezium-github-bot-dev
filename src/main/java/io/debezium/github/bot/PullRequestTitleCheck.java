/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.github.bot;

import java.io.IOException;
import java.util.regex.Pattern;

import io.debezium.github.bot.check.Check;
import io.debezium.github.bot.check.CheckContext;
import io.debezium.github.bot.check.CheckRunOutput;

/**
 * A check that validates that the pull request title adheres to the following rules:
 *
 * <ul>
 *      <li>Title contains at least 2 words</li>
 *      <li>Title does not end with the ellipsis</li>
 * </ul>
 *
 * @author Chris Cranford
 */
public class PullRequestTitleCheck extends Check {

    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
    private static final String TWO_WORD_RULE = "The pull request title should contain at least 2 words";
    private static final String NO_ELLIPSIS_RULE = "The pull request title should not end with an ellipsis";

    PullRequestTitleCheck() {
        super("Pull Request Title");
    }

    @Override
    public void run(CheckContext context, CheckRunOutput output) throws IOException {
        String title = context.pullRequest.getTitle();
        output.rule(TWO_WORD_RULE).result(title != null && SPACE_PATTERN.split(title.trim()).length >= 2);
        output.rule(NO_ELLIPSIS_RULE).result(title != null && !title.endsWith("..."));
    }
}
