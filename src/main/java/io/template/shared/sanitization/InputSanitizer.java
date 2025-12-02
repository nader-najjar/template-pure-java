package io.template.shared.sanitization;


import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.inject.Inject;
import io.template.shared.exceptions.InvalidInputException;
import io.template.shared.models.ApplicationInput;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Handles input sanitization.
 * Deserializes raw input strings into ApplicationInput objects and validates them.
 */
public class InputSanitizer {

    private final JsonMapper jsonMapper;
    private final Validator validator;

    @Inject
    public InputSanitizer(JsonMapper jsonMapper, Validator validator) {
        this.jsonMapper = jsonMapper;
        this.validator = validator;
    }

    /**
     * Parses and validates raw input arguments.
     *
     * @param args raw input arguments
     * @return parsed and validated ApplicationInput
     * @throws IllegalArgumentException if input is missing or invalid
     */
    public ApplicationInput sanitize(String[] args) {
        validateArgumentsStructure(args);
        String jsonString = args[0];

        ApplicationInput applicationInput;
        try {
            applicationInput = jsonMapper.readValue(jsonString, ApplicationInput.class);
        } catch (JsonProcessingException e) {
            throw new InvalidInputException("Invalid input JSON: ", e);
        }

        validateDeserializedInput(applicationInput);
        return applicationInput;
    }

    private void validateArgumentsStructure(String[] args) {
        if (args == null || args.length == 0) {
            throw new InvalidInputException("No input provided");
        }
    }

    private void validateDeserializedInput(ApplicationInput input) {
        Set<ConstraintViolation<ApplicationInput>> violations = validator.validate(input);

        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));

            throw new InvalidInputException("Validation failed: " + errors);
        }
    }
}
