package io.template;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application entry point.
 * Creates the dependency injection container and starts execution.
 */
public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Main() {
        // Prevent instantiation
    }

    public static void main(String[] args) {
        try {
            // TODO add tests w cursor

            Injector injector = Guice.createInjector(new InjectionModule());
            LifecycleManager.registerShutdownHooks(injector);

            Executor executor = injector.getInstance(Executor.class);
            executor.execute(args);
        } catch (Exception e) {
            LOGGER.error("Technical exception occurred: ", e);
            safeCleanup();
            System.exit(1);
        }
    }

    private static void safeCleanup() {
        // Execute any cleanup code that must be done upon failure
    }
}
