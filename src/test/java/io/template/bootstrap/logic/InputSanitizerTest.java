package io.template.bootstrap.logic;

import java.util.Set;

import io.template.bootstrap.exceptions.InvalidInputException;
import io.template.shared.models.ApplicationInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputSanitizerTest {

    private InputSanitizer sanitizer;

    @BeforeEach
    void setUp() {
        sanitizer = new InputSanitizer();
    }

    @Test
    void sanitizesValidInput() {
        String json = """
                {
                  "exampleStringField": "hello",
                  "exampleIntField": 3,
                  "exampleBooleanField": true,
                  "exampleTimestampField": "2024-01-01T00:00:00Z",
                  "exampleListField": ["a","b"]
                }
                """;

        ApplicationInput input = sanitizer.sanitize(new String[]{json});

        assertEquals("hello", input.exampleStringField());
        assertEquals(3, input.exampleIntField());
        assertTrue(input.exampleBooleanField());
        assertEquals("2024-01-01T00:00:00Z", input.exampleTimestampField().toString());
        assertEquals(Set.of("a", "b"), Set.copyOf(input.exampleListField()));
    }

    @Test
    void sanitizesInputWithFalseBoolean() {
        String json = """
                {
                  "exampleStringField": "test",
                  "exampleIntField": 0,
                  "exampleBooleanField": false,
                  "exampleTimestampField": "2024-01-01T00:00:00Z",
                  "exampleListField": []
                }
                """;

        ApplicationInput input = sanitizer.sanitize(new String[]{json});

        assertFalse(input.exampleBooleanField());
        assertEquals(0, input.exampleIntField());
        assertTrue(input.exampleListField().isEmpty());
    }

    @Test
    void rejectsNullArgs() {
        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> sanitizer.sanitize(null)
        );

        assertTrue(exception.getMessage().contains("No input provided"));
    }

    @Test
    void rejectsEmptyArgs() {
        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> sanitizer.sanitize(new String[]{})
        );

        assertTrue(exception.getMessage().contains("No input provided"));
    }

    @Test
    void rejectsInvalidJson() {
        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> sanitizer.sanitize(new String[]{"{"})
        );

        assertTrue(exception.getMessage().contains("Invalid input JSON"));
        assertNotNull(exception.getCause());
    }

    @Test
    void rejectsEmptyJsonString() {
        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> sanitizer.sanitize(new String[]{""})
        );

        assertTrue(exception.getMessage().contains("Invalid input JSON"));
    }

    @Test
    void rejectsMalformedJson() {
        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> sanitizer.sanitize(new String[]{"not json at all"})
        );

        assertTrue(exception.getMessage().contains("Invalid input JSON"));
    }

    @Test
    void rejectsJsonWithInvalidFieldTypes() {
        String json = """
                {
                  "exampleStringField": "hello",
                  "exampleIntField": "not-a-number",
                  "exampleBooleanField": true,
                  "exampleTimestampField": "2024-01-01T00:00:00Z",
                  "exampleListField": ["a","b"]
                }
                """;

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> sanitizer.sanitize(new String[]{json})
        );

        assertTrue(exception.getMessage().contains("Invalid input JSON"));
        assertNotNull(exception.getCause());
    }

    @Test
    void rejectsJsonWithInvalidTimestampFormat() {
        String json = """
                {
                  "exampleStringField": "hello",
                  "exampleIntField": 3,
                  "exampleBooleanField": true,
                  "exampleTimestampField": "invalid-date",
                  "exampleListField": ["a","b"]
                }
                """;

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> sanitizer.sanitize(new String[]{json})
        );

        assertTrue(exception.getMessage().contains("Invalid input JSON"));
        assertNotNull(exception.getCause());
    }

    @Test
    void usesFirstArgWhenMultipleArgsProvided() {
        String json1 = """
                {
                  "exampleStringField": "first",
                  "exampleIntField": 1,
                  "exampleBooleanField": true,
                  "exampleTimestampField": "2024-01-01T00:00:00Z",
                  "exampleListField": ["a"]
                }
                """;
        String json2 = """
                {
                  "exampleStringField": "second",
                  "exampleIntField": 2,
                  "exampleBooleanField": false,
                  "exampleTimestampField": "2024-01-01T00:00:00Z",
                  "exampleListField": ["b"]
                }
                """;

        ApplicationInput input = sanitizer.sanitize(new String[]{json1, json2});

        assertEquals("first", input.exampleStringField());
        assertEquals(1, input.exampleIntField());
    }

    @Test
    void handlesJsonWithNullStringField() {
        String json = """
                {
                  "exampleStringField": null,
                  "exampleIntField": 3,
                  "exampleBooleanField": true,
                  "exampleTimestampField": "2024-01-01T00:00:00Z",
                  "exampleListField": ["a","b"]
                }
                """;

        ApplicationInput input = sanitizer.sanitize(new String[]{json});

        assertNotNull(input);
        // null string field is allowed (no validation constraints)
    }
}
