package io.template.bootstrap.injectionmodules;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.template.bootstrap.exceptions.EnvironmentVariableException;
import io.template.shared.models.EnvironmentVariables;
import io.template.shared.utilities.HibernateValidatorUtility;
import jakarta.validation.ConstraintViolation;

public class EnvironmentModule extends AbstractModule {

    private final Map<String, String> environment;

    public EnvironmentModule() {
        this(System.getenv());
    }

    @VisibleForTesting
    public EnvironmentModule(Map<String, String> environment) {
        this.environment = environment;
    }

    @Provides
    @Singleton
    EnvironmentVariables provideEnvironmentVariables() {
        return parseEnvironmentVariables(environment);
    }

    private EnvironmentVariables parseEnvironmentVariables(Map<String, String> environment) {
        EnvironmentVariables environmentVariables = new EnvironmentVariables(
                extractString(environment, "STAGE"),
                extractString(environment, "REGION"),
                extractString(environment, "EXAMPLE_STRING_VAR"),
                extractInt(environment, "EXAMPLE_INT_VAR"),
                extractBoolean(environment, "EXAMPLE_BOOLEAN_VAR")
        );

        validateEnvironmentVariables(environmentVariables);

        return environmentVariables;
    }

    private void validateEnvironmentVariables(EnvironmentVariables environmentVariables) {
        Set<ConstraintViolation<EnvironmentVariables>> violations = HibernateValidatorUtility.VALIDATOR
                .validate(environmentVariables);

        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));

            throw new EnvironmentVariableException("Environment validation failed: " + errors);
        }
    }

    private String extractString(Map<String, String> environment, String key) {
        String value = environment.get(key);
        ensureVariableExists(key, value);
        return value;
    }

    private int extractInt(Map<String, String> environment, String key) {
        String value = environment.get(key);
        ensureVariableExists(key, value);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            String message = "Environment variable '" + key + "' must be a valid integer, got: " + value;
            throw new EnvironmentVariableException(message, e);
        }
    }

    private boolean extractBoolean(Map<String, String> environment, String key) {
        String value = environment.get(key);
        ensureVariableExists(key, value);

        return customParseBoolean(key, value);
    }

    private void ensureVariableExists(String key, String value) {
        if (value == null || value.isBlank()) {
            throw new EnvironmentVariableException("Required environment variable '" + key + "' is not set");
        }
    }

    /**
     * The existing Boolean.parseBoolean() returns `false` for **any** value not equal to `true`.
     * We want an error thrown if the value is unexpected.
     * Further, we abide by the JSON standard of expecting lowercase `true` or `false`.
     */
    private boolean customParseBoolean(String key, String value) {
        if ("true".equals(value)) {
            return true;
        }
        if ("false".equals(value)) {
            return false;
        }

        String message = "Environment variable '" + key + "' must be 'true' or 'false', got: " + value;
        throw new EnvironmentVariableException(message);
    }
}
