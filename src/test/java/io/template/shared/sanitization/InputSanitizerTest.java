package io.template.shared.sanitization;

import java.util.Set;

import io.template.shared.exceptions.InvalidInputException;
import io.template.shared.models.ApplicationInput;
import io.template.shared.utilities.HibernateValidatorFactory;
import io.template.shared.utilities.JsonMapperFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class InputSanitizerTest {

    private InputSanitizer sanitizer;

    @BeforeEach
    void setUp() {
        sanitizer = new InputSanitizer(
                JsonMapperFactory.createStrictMapper(),
                HibernateValidatorFactory.createValidator()
        );
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
    void rejectsInvalidJson() {
        assertThrows(InvalidInputException.class, () -> sanitizer.sanitize(new String[]{"{"}));
    }

    @Test
    void rejectsMissingArgs() {
        assertThrows(InvalidInputException.class, () -> sanitizer.sanitize(new String[]{}));
    }
}
