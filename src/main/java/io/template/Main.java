package io.template;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.template.bootstrap.injectionmodules.EnvironmentModule;
import io.template.bootstrap.logic.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application entry point.
 * Follows idempotency-based lifecycle management - all cleanup happens in main thread.
 * See: temp-docs/lifecycle-management-paradigm.md
 *
 * The following classes must not have associated unit tests - smoke tests are used in their place:
 *   - `Main.java`
 *   - `LifecycleManager.java`
 *   - `bootstrap/injectionmodules/*`
 */
public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Main() { }

    public static void main(String[] args) {
        // Register shutdown hook (only sets flag, doesn't cleanup)
        LifecycleManager.registerShutdownHook();

        Injector injector = null;

        try {
            injector = Guice.createInjector(
                    new EnvironmentModule()
            );

            Executor executor = injector.getInstance(Executor.class);
            executor.execute(args);

            if (LifecycleManager.isShutdownRequested()) {
                LOGGER.info("Graceful shutdown: execution completed after shutdown request");
            }

        } catch (Exception exception) {
            LOGGER.error("Technical exception occurred at software entrypoint level: ", exception);
            System.exit(1);
        } finally {
            // CLEANUP HAPPENS HERE - in main thread, predictable order
            LOGGER.debug("Cleaning up resources...");
            cleanupResources(injector);
            LOGGER.debug("Cleanup complete, exiting");
        }
    }

    /**
     * Cleans up resources that require explicit shutdown.
     * This method is called in the main thread's finally block, ensuring
     * predictable cleanup order without race conditions.
     *
     * @param injector the Guice injector containing managed resources (may be null)
     */
    private static void cleanupResources(Injector injector) {
        if (injector == null) {
            return;
        }

        try {
            // Pull AutoCloseable resources from the injector and close them here
            // Example:
            // SomeResource resource = injector.getInstance(SomeResource.class);
            // if (resource instanceof AutoCloseable) {
            //     ((AutoCloseable) resource).close();
            // }
        } catch (Exception e) {
            LOGGER.error("Error during resource cleanup", e);
        }
    }
}
