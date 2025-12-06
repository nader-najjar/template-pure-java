package io.template.bootstrap.logic;

import java.time.Instant;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.template.bootstrap.exceptions.InvalidInputException;
import io.template.samplebusinesslayer.logic.Calculator;
import io.template.shared.models.ApplicationInput;
import io.template.shared.models.EnvironmentVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutorTest {

    @Mock
    private InputSanitizer inputSanitizer;

    @Mock
    private Calculator calculator;

    @Mock
    private EnvironmentVariables environmentVariables;

    private Executor executor;

    @BeforeEach
    void setUp() {
        when(environmentVariables.stage()).thenReturn("test");
        when(environmentVariables.region()).thenReturn("us-east-1");

        executor = new Executor(environmentVariables, inputSanitizer, calculator);
    }

    @Test
    void executesWithValidInput() {
        String json = """
                {
                  "exampleStringField": "test",
                  "exampleIntField": 1,
                  "exampleBooleanField": true,
                  "exampleTimestampField": "2024-01-01T00:00:00Z",
                  "exampleListField": ["a"]
                }
                """;
        String[] args = new String[]{json};
        ApplicationInput mockInput = new ApplicationInput(
                "test",
                1,
                true,
                Instant.parse("2024-01-01T00:00:00Z"),
                List.of("a")
        );

        when(inputSanitizer.sanitize(args)).thenReturn(mockInput);

        executor.execute(args);

        // Verify environment variables are accessed
        verify(environmentVariables).stage();
        verify(environmentVariables).region();

        // Verify input sanitization
        verify(inputSanitizer).sanitize(args);

        // Verify calculator is called with the exact expected request
        verify(calculator).calculate(argThat(request ->
                request.operandA() == 10.0 &&
                request.operandB() == 5.0 &&
                "ADD".equals(request.operation())
        ));
    }

    @SuppressFBWarnings(
            value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
            justification = "Mockito in-order verification; return values are irrelevant"
    )
    @Test
    void executesInCorrectOrder() {
        String json = """
                {
                  "exampleStringField": "test",
                  "exampleIntField": 1,
                  "exampleBooleanField": true,
                  "exampleTimestampField": "2024-01-01T00:00:00Z",
                  "exampleListField": ["a"]
                }
                """;
        String[] args = new String[]{json};
        ApplicationInput mockInput = new ApplicationInput(
                "test",
                1,
                true,
                Instant.parse("2024-01-01T00:00:00Z"),
                List.of("a")
        );

        when(inputSanitizer.sanitize(args)).thenReturn(mockInput);

        executor.execute(args);

        // Verify the order of operations
        InOrder inOrder = inOrder(environmentVariables, inputSanitizer, calculator);
        inOrder.verify(environmentVariables).stage();
        inOrder.verify(environmentVariables).region();
        inOrder.verify(inputSanitizer).sanitize(args);
        inOrder.verify(calculator).calculate(argThat(request ->
                request.operandA() == 10.0 &&
                request.operandB() == 5.0 &&
                "ADD".equals(request.operation())
        ));
    }

    @Test
    void propagatesExceptionWhenInputSanitizerFails() {
        String[] args = new String[]{"invalid-json"};
        InvalidInputException exception = new InvalidInputException("Invalid input JSON");

        when(inputSanitizer.sanitize(args)).thenThrow(exception);

        assertThrows(
                InvalidInputException.class,
                () -> executor.execute(args)
        );

        // Verify environment variables are still accessed (logging happens before sanitization)
        verify(environmentVariables).stage();
        verify(environmentVariables).region();
        verify(inputSanitizer).sanitize(args);
        // Verify calculator is never called when sanitization fails
        verify(calculator, never()).calculate(argThat(request -> true));
    }
}
