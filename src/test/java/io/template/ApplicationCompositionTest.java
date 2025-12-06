package io.template;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.template.bootstrap.injectionmodules.EnvironmentModule;
import io.template.bootstrap.logic.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ApplicationCompositionTest {

    private Map<String, String> environment;

    @BeforeEach
    void setUp() {
        environment = new HashMap<>();
        environment.put("STAGE", "composition");
        environment.put("REGION", "us-west-2");
        environment.put("EXAMPLE_STRING_VAR", "example");
        environment.put("EXAMPLE_INT_VAR", "7");
        environment.put("EXAMPLE_BOOLEAN_VAR", "true");
    }

    @Test
    void assemblesAndRunsApplicationWithValidEnvironment() {
        Injector injector = Guice.createInjector(new EnvironmentModule(environment));
        Executor executor = injector.getInstance(Executor.class);

        String jsonInput = """
                {
                  "exampleStringField": "hello",
                  "exampleIntField": 123,
                  "exampleBooleanField": true,
                  "exampleTimestampField": "2024-01-01T00:00:00Z",
                  "exampleListField": ["x", "y"]
                }
                """;
        String[] args = new String[]{jsonInput};

        assertDoesNotThrow(() -> executor.execute(args));
    }
}
