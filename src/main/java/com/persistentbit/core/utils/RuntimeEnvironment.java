package com.persistentbit.core.utils;

/**
 * Enum for representing the runtime for an application or service.<br>
 */
public enum RuntimeEnvironment {
    /**
     * Run in production.
     */
    production,
    /**
     * Run in test.
     */
    test,
    /**
     * Run in unit tests.
     */
    unitTest,
    /**
     * Run on local development machine
     */
    development
}
