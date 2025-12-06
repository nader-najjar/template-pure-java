package io.template.bootstrap.injectionmodules;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import io.template.bootstrap.exceptions.EnvironmentVariableException;
import io.template.shared.models.EnvironmentVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentModuleTest {

    private Map<String, String> environment;

    @BeforeEach
    void setUp() {
        environment = new HashMap<>();
    }

    @Test
    void providesEnvironmentVariablesWithValidValues() {
        environment.put("STAGE", "production");
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_STRING_VAR", "test-value");
        environment.put("EXAMPLE_INT_VAR", "42");
        environment.put("EXAMPLE_BOOLEAN_VAR", "true");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariables result = injector.getInstance(EnvironmentVariables.class);

        assertNotNull(result);
        assertEquals("production", result.stage());
        assertEquals("us-east-1", result.region());
        assertEquals("test-value", result.exampleStringVar());
        assertEquals(42, result.exampleIntVar());
        assertTrue(result.exampleBooleanVar());
    }

    @Test
    void providesEnvironmentVariablesWithFalseBoolean() {
        environment.put("STAGE", "dev");
        environment.put("REGION", "us-west-2");
        environment.put("EXAMPLE_STRING_VAR", "value");
        environment.put("EXAMPLE_INT_VAR", "100");
        environment.put("EXAMPLE_BOOLEAN_VAR", "false");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariables result = injector.getInstance(EnvironmentVariables.class);

        assertFalse(result.exampleBooleanVar());
        assertEquals("dev", result.stage());
        assertEquals(100, result.exampleIntVar());
    }

    @Test
    void throwsExceptionWhenStageIsMissing() {
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_STRING_VAR", "test");
        environment.put("EXAMPLE_INT_VAR", "1");
        environment.put("EXAMPLE_BOOLEAN_VAR", "true");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariableException exception = expectEnvironmentException(
                () -> injector.getInstance(EnvironmentVariables.class)
        );

        assertTrue(exception.getMessage().contains("STAGE"));
        assertTrue(exception.getMessage().contains("not set"));
    }

    @Test
    void throwsExceptionWhenRegionIsMissing() {
        environment.put("STAGE", "prod");
        environment.put("EXAMPLE_STRING_VAR", "test");
        environment.put("EXAMPLE_INT_VAR", "1");
        environment.put("EXAMPLE_BOOLEAN_VAR", "true");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariableException exception = expectEnvironmentException(
                () -> injector.getInstance(EnvironmentVariables.class)
        );

        assertTrue(exception.getMessage().contains("REGION"));
        assertTrue(exception.getMessage().contains("not set"));
    }

    @Test
    void throwsExceptionWhenExampleStringVarIsMissing() {
        environment.put("STAGE", "prod");
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_INT_VAR", "1");
        environment.put("EXAMPLE_BOOLEAN_VAR", "true");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariableException exception = expectEnvironmentException(
                () -> injector.getInstance(EnvironmentVariables.class)
        );

        assertTrue(exception.getMessage().contains("EXAMPLE_STRING_VAR"));
        assertTrue(exception.getMessage().contains("not set"));
    }

    @Test
    void throwsExceptionWhenExampleIntVarIsMissing() {
        environment.put("STAGE", "prod");
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_STRING_VAR", "test");
        environment.put("EXAMPLE_BOOLEAN_VAR", "true");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariableException exception = expectEnvironmentException(
                () -> injector.getInstance(EnvironmentVariables.class)
        );

        assertTrue(exception.getMessage().contains("EXAMPLE_INT_VAR"));
        assertTrue(exception.getMessage().contains("not set"));
    }

    @Test
    void throwsExceptionWhenExampleBooleanVarIsMissing() {
        environment.put("STAGE", "prod");
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_STRING_VAR", "test");
        environment.put("EXAMPLE_INT_VAR", "1");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariableException exception = expectEnvironmentException(
                () -> injector.getInstance(EnvironmentVariables.class)
        );

        assertTrue(exception.getMessage().contains("EXAMPLE_BOOLEAN_VAR"));
        assertTrue(exception.getMessage().contains("not set"));
    }

    @Test
    void throwsExceptionWhenVariableIsBlank() {
        environment.put("STAGE", "");
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_STRING_VAR", "test");
        environment.put("EXAMPLE_INT_VAR", "1");
        environment.put("EXAMPLE_BOOLEAN_VAR", "true");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariableException exception = expectEnvironmentException(
                () -> injector.getInstance(EnvironmentVariables.class)
        );

        assertTrue(exception.getMessage().contains("STAGE"));
        assertTrue(exception.getMessage().contains("not set"));
    }

    @Test
    void throwsExceptionWhenVariableIsWhitespace() {
        environment.put("STAGE", "   ");
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_STRING_VAR", "test");
        environment.put("EXAMPLE_INT_VAR", "1");
        environment.put("EXAMPLE_BOOLEAN_VAR", "true");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariableException exception = expectEnvironmentException(
                () -> injector.getInstance(EnvironmentVariables.class)
        );

        assertTrue(exception.getMessage().contains("STAGE"));
        assertTrue(exception.getMessage().contains("not set"));
    }

    @Test
    void throwsExceptionWhenIntVarIsNotANumber() {
        environment.put("STAGE", "prod");
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_STRING_VAR", "test");
        environment.put("EXAMPLE_INT_VAR", "not-a-number");
        environment.put("EXAMPLE_BOOLEAN_VAR", "true");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariableException exception = expectEnvironmentException(
                () -> injector.getInstance(EnvironmentVariables.class)
        );

        assertTrue(exception.getMessage().contains("EXAMPLE_INT_VAR"));
        assertTrue(exception.getMessage().contains("valid integer"));
        assertTrue(exception.getMessage().contains("not-a-number"));
        assertNotNull(exception.getCause());
    }

    @Test
    void throwsExceptionWhenIntVarIsDecimal() {
        environment.put("STAGE", "prod");
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_STRING_VAR", "test");
        environment.put("EXAMPLE_INT_VAR", "3.14");
        environment.put("EXAMPLE_BOOLEAN_VAR", "true");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariableException exception = expectEnvironmentException(
                () -> injector.getInstance(EnvironmentVariables.class)
        );

        assertTrue(exception.getMessage().contains("EXAMPLE_INT_VAR"));
        assertTrue(exception.getMessage().contains("valid integer"));
    }

    @Test
    void throwsExceptionWhenBooleanVarIsNotTrueOrFalse() {
        environment.put("STAGE", "prod");
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_STRING_VAR", "test");
        environment.put("EXAMPLE_INT_VAR", "1");
        environment.put("EXAMPLE_BOOLEAN_VAR", "yes");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariableException exception = expectEnvironmentException(
                () -> injector.getInstance(EnvironmentVariables.class)
        );

        assertTrue(exception.getMessage().contains("EXAMPLE_BOOLEAN_VAR"));
        assertTrue(exception.getMessage().contains("'true' or 'false'"));
        assertTrue(exception.getMessage().contains("yes"));
    }

    @Test
    void throwsExceptionWhenBooleanVarIsTrueWithDifferentCase() {
        environment.put("STAGE", "prod");
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_STRING_VAR", "test");
        environment.put("EXAMPLE_INT_VAR", "1");
        environment.put("EXAMPLE_BOOLEAN_VAR", "True");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariableException exception = expectEnvironmentException(
                () -> injector.getInstance(EnvironmentVariables.class)
        );

        assertTrue(exception.getMessage().contains("EXAMPLE_BOOLEAN_VAR"));
        assertTrue(exception.getMessage().contains("'true' or 'false'"));
    }

    @Test
    void throwsExceptionWhenBooleanVarIsFalseWithDifferentCase() {
        environment.put("STAGE", "prod");
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_STRING_VAR", "test");
        environment.put("EXAMPLE_INT_VAR", "1");
        environment.put("EXAMPLE_BOOLEAN_VAR", "FALSE");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariableException exception = expectEnvironmentException(
                () -> injector.getInstance(EnvironmentVariables.class)
        );

        assertTrue(exception.getMessage().contains("EXAMPLE_BOOLEAN_VAR"));
        assertTrue(exception.getMessage().contains("'true' or 'false'"));
    }

    @Test
    void throwsExceptionWhenBooleanVarIsOne() {
        environment.put("STAGE", "prod");
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_STRING_VAR", "test");
        environment.put("EXAMPLE_INT_VAR", "1");
        environment.put("EXAMPLE_BOOLEAN_VAR", "1");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariableException exception = expectEnvironmentException(
                () -> injector.getInstance(EnvironmentVariables.class)
        );

        assertTrue(exception.getMessage().contains("EXAMPLE_BOOLEAN_VAR"));
        assertTrue(exception.getMessage().contains("'true' or 'false'"));
    }

    @Test
    void handlesNegativeInteger() {
        environment.put("STAGE", "prod");
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_STRING_VAR", "test");
        environment.put("EXAMPLE_INT_VAR", "-42");
        environment.put("EXAMPLE_BOOLEAN_VAR", "true");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariables result = injector.getInstance(EnvironmentVariables.class);

        assertEquals(-42, result.exampleIntVar());
    }

    @Test
    void handlesZeroInteger() {
        environment.put("STAGE", "prod");
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_STRING_VAR", "test");
        environment.put("EXAMPLE_INT_VAR", "0");
        environment.put("EXAMPLE_BOOLEAN_VAR", "true");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariables result = injector.getInstance(EnvironmentVariables.class);

        assertEquals(0, result.exampleIntVar());
    }

    @Test
    void handlesLargeInteger() {
        environment.put("STAGE", "prod");
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_STRING_VAR", "test");
        environment.put("EXAMPLE_INT_VAR", "2147483647");
        environment.put("EXAMPLE_BOOLEAN_VAR", "true");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariables result = injector.getInstance(EnvironmentVariables.class);

        assertEquals(Integer.MAX_VALUE, result.exampleIntVar());
    }

    @Test
    void providesSingletonInstance() {
        environment.put("STAGE", "prod");
        environment.put("REGION", "us-east-1");
        environment.put("EXAMPLE_STRING_VAR", "test");
        environment.put("EXAMPLE_INT_VAR", "1");
        environment.put("EXAMPLE_BOOLEAN_VAR", "true");

        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        EnvironmentVariables first = injector.getInstance(EnvironmentVariables.class);
        EnvironmentVariables second = injector.getInstance(EnvironmentVariables.class);

        assertSame(first, second, "EnvironmentVariables should be a singleton");
    }

    private EnvironmentVariableException expectEnvironmentException(Executable executable) {
        ProvisionException provisionException = assertThrows(ProvisionException.class, executable);
        Throwable cause = provisionException.getCause();
        assertTrue(cause instanceof EnvironmentVariableException, "Expected EnvironmentVariableException as cause");
        return (EnvironmentVariableException) cause;
    }
}
