/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.github.bot.check;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The output of a CheckRun.
 *
 * @author Chris Cranford
 */
public class CheckRunOutput {

    private final List<CheckRunRule> rules = new ArrayList<>();

    public final long id;
    public final String name;

    CheckRunOutput(long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Create a rule by description
     *
     * @param description the description of the run to be displayed
     * @return the rule; never null
     */
    public CheckRunRule rule(String description) {
        CheckRunRule rule = new CheckRunRule(description);
        rules.add(rule);
        return rule;
    }

    /**
     * Checks whether all rules passed; one failure causes the run to have failed.
     *
     * @return true if all rules passed; false otherwise
     */
    public boolean passed() {
        return rules.stream().allMatch(r -> r.passed);
    }

    /**
     * Get the title that should be displayed for the output.
     *
     * @return the title
     */
    public final String title() {
        List<CheckRunRule> failingRules = rules.stream().filter(r -> !r.passed).collect(Collectors.toList());
        if (failingRules.isEmpty()) {
            return "All rules passed";
        }
        else if (failingRules.size() == 1) {
            return failingRules.get(0).description;
        }
        else {
            return failingRules.size() + " rules failed";
        }
    }

    /**
     * Get the contents that should be displayed in the output.
     *
     * @return the contents
     */
    public final String contents() {
        StringBuilder builder = new StringBuilder();
        appendRules(builder, true);
        return builder.toString();
    }

    /**
     * Iterates all rules and appends the output of each rule to the builder if the rule failed.
     * If the rule passed; the rule will be excluded from the builder's output unless the rule
     * explicitly flags itself to always be included.
     *
     * @param builder the builder; must not be null
     */
    public void appendFailingRules(StringBuilder builder) {
        appendRules(builder, false);
    }

    private void appendRules(StringBuilder builder, boolean includePassed) {
        for (CheckRunRule rule : rules) {
            // Check whether rule should be included
            if (rule.passed && !includePassed && !rule.alwaysIncluded) {
                continue;
            }

            // Gather the icon; preferring the rule's overridden icon from those defined here
            String icon = rule.icon != null ? rule.icon : (rule.passed ? "✔" : "❌");

            // Add the description
            builder.append("\n").append(icon).append("\u00A0").append(rule.description);

            // If there are comments, add each one as one line below each entry
            for (String comment : rule.comments) {
                builder.append("\n\u00A0\u00A0\u00A0\u00A0↳\u00A0").append(comment);
            }
        }
    }
}
