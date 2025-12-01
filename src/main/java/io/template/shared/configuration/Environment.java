package io.template.shared.configuration;

import io.template.shared.exceptions.MissingEnvironmentVariableException;
import io.template.shared.models.EnvironmentVariables;

/**
 * Global environment configuration singleton.
 * Parses environment variables once at startup.
 * Throws MissingEnvironmentVariableException if any required variable is missing or invalid.
 */
public final class Environment {

    private static final EnvironmentVariables INSTANCE = loadEnvironmentVariables();

    private Environment() {
        // Prevent instantiation
    }

    public static EnvironmentVariables get() {
        return INSTANCE;
    }

    private static EnvironmentVariables loadEnvironmentVariables() {
        return new EnvironmentVariables(
                getRequiredEnv("STAGE"),
                getRequiredEnv("REGION"),
                getRequiredEnv("EXAMPLE_STRING_VAR"),
                getRequiredEnvInt("EXAMPLE_INT_VAR"),
                getRequiredEnvBoolean("EXAMPLE_BOOLEAN_VAR")
        );
    }

    private static String getRequiredEnv(String key) {
        String value = System.getenv(key);

        if (value == null || value.isBlank()) {
            throw new MissingEnvironmentVariableException("Required environment variable '" + key + "' is not set");
        }

        return value;
    }

    private static int getRequiredEnvInt(String key) {
        String value = getRequiredEnv(key);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            String message = "Environment variable '" + key + "' must be a valid integer, got: " + value;
            throw new MissingEnvironmentVariableException(message, e);
        }
    }

    private static boolean getRequiredEnvBoolean(String key) {
        String value = getRequiredEnv(key);

        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        String message = "Environment variable '" + key + "' must be 'true' or 'false', got: " + value;
        throw new MissingEnvironmentVariableException(message);
    }
}
