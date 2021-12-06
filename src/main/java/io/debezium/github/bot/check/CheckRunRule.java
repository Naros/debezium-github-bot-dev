/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.github.bot.check;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a specific GitHub check rule that will be displayed in the pull request comments.  A rule
 * defines a description, optional comments and icon along with whether the rule passed or failed.
 *
 * @author Chris Cranford
 */
public class CheckRunRule {

    final String description;
    final List<String> comments = new ArrayList<>();

    boolean passed;
    boolean alwaysIncluded;
    String icon;

    /**
     * Create a check rule.
     *
     * @param description the description of the rule
     */
    public CheckRunRule(String description) {
        this.description = description;
    }

    public void icon(String icon) {
        this.icon = icon;
    }

    /**
     * Mark a rule to always be included in the output.
     *
     * In some scenarios the check may only want to output failures but marking a rule as always
     * allows per-rule overrides of this behavior for it to be included regardless.
     */
    public void always() {
        this.alwaysIncluded = true;
    }

    /**
     * Mark a rule as passed or failed.
     *
     * @param passed whether the rule passed or failed.
     */
    public void result(boolean passed) {
        this.passed = passed;
    }

    /**
     * Mark a rule as passed or failed with a single-line response.
     *
     * @param passed whether the rule passed or not
     * @param comment the single line comment to add to the output message.
     */
    public void result(boolean passed, String comment) {
        this.passed = passed;
        this.comments.add(comment);
    }

    /**
     * Mark a rule as passed or failed with a multi-line response.
     *
     * @param passed whether the rule passed or not
     * @param comments the multi line comments to add to the output message.
     */
    public void result(boolean passed, List<String> comments) {
        this.passed = passed;
        this.comments.addAll(comments);
    }

    /**
     * Mark a rule as passed without any response.
     */
    public void passed() {
        this.passed = true;
        this.comments.clear();
    }

    /**
     * Mark a rule as passed with a single line response.
     *
     * @param comment the comment to add to the output message.
     */
    public void passed(String comment) {
        this.passed = true;
        this.comments.add(comment);
    }

    /**
     * Mark a rule as passed with a multi-line response.
     *
     * @param comments a list of comments to be added to the output message.
     */
    public void passed(List<String> comments) {
        this.passed = true;
        this.comments.addAll(comments);
    }

    /**
     * Mark a rule failed with a single line response.
     *
     * @param comment the comment to add to the output message.
     */
    public void failed(String comment) {
        this.passed = false;
        this.comments.add(comment);
    }

    /**
     * Mark a rule as failed with a multi-line response.
     *
     * @param comments a list of comments to be added to the output message.
     */
    public void failed(List<String> comments) {
        this.passed = false;
        this.comments.addAll(comments);
    }
}
