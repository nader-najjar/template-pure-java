package io.template;

import com.google.inject.Inject;
import io.template.samplebusinesslayer.logic.Calculator;
import io.template.samplebusinesslayer.models.CalculationRequest;
import io.template.shared.configuration.Environment;
import io.template.shared.models.ApplicationInput;
import io.template.shared.models.EnvironmentVariables;
import io.template.shared.sanitization.InputSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main business logic executor.
 * Contains the core application logic.
 */
public class Executor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Executor.class);

    private final InputSanitizer inputSanitizer;
    private final Calculator calculator;

    @Inject
    public Executor(InputSanitizer inputSanitizer, Calculator calculator) {
        this.inputSanitizer = inputSanitizer;
        this.calculator = calculator;
    }

    public void execute(String[] args) {
        EnvironmentVariables env = Environment.get();
        LOGGER.info("Executing with stage: {}, region: {}", env.stage(), env.region());

        ApplicationInput input = inputSanitizer.sanitize(args);
        LOGGER.info("Sanitized input: {}", input);

        invokeSampleLogic();
    }

    private void invokeSampleLogic() {
        CalculationRequest request = new CalculationRequest(10.0, 5.0, "ADD");
        calculator.calculate(request);
    }
}
