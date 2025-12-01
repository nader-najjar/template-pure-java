package io.template.shared.models;

import java.time.Instant;
import java.util.List;

/**
 * Application input model.
 * Represents the deserialized input received by the application.
 * Customize fields based on your application's needs.
 *
 * @param exampleStringField Example string field
 * @param exampleIntField Example integer field
 * @param exampleBooleanField Example boolean field
 * @param exampleTimestampField Example timestamp field
 * @param exampleListField Example list field
 */
public record ApplicationInput(
        String exampleStringField,
        int exampleIntField,
        boolean exampleBooleanField,
        Instant exampleTimestampField,
        List<String> exampleListField
) { }
