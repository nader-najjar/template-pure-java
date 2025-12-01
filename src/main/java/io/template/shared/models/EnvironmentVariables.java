package io.template.shared.models;

/**
 * Environment variables configuration.
 *
 * @param stage Deployment stage (e.g., dev, staging, prod)
 * @param region Deployment region (e.g., us-west-2, us-east-1)
 * @param exampleStringVar Example string environment variable
 * @param exampleIntVar Example integer environment variable
 * @param exampleBooleanVar Example boolean environment variable
 */
public record EnvironmentVariables(
        String stage,
        String region,
        String exampleStringVar,
        int exampleIntVar,
        boolean exampleBooleanVar
) { }
