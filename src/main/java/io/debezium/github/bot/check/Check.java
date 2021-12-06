/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.github.bot.check;

import java.io.IOException;

/**
 * The base abstract class for a check performed by the bot.
 *
 * @author Chris Cranford
 */
public abstract class Check {

    final String name;

    /**
     * Constructs a check with a name.
     *
     * @param name the name of the check
     */
    protected Check(String name) {
        this.name = name;
    }

    /**
     * Executes the check.
     *
     * @param context the context, must not be null
     * @param output the output collector, must not be null
     * @throws IOException if an error occurred
     */
    public abstract void run(CheckContext context, CheckRunOutput output) throws IOException;

    /**
     * Helper method to create a check execution's output.
     *
     * @param context the context, must not be null
     * @param check the check to execute, must not be null
     * @return the output from the check
     * @throws IOException if an error occurred
     */
    public static CheckRunOutput run(CheckContext context, Check check) throws IOException {
        CheckRun run = CheckRun.create(context, check);
        return run.run();
    }
}
