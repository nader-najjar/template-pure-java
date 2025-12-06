package io.template.bootstrap.logic;

import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages application lifecycle and resource cleanup.
 * Handles shutdown hooks for AutoCloseable resources managed by Guice.
 */
public final class LifecycleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleManager.class);

    private LifecycleManager() { }

    /**
     * Registers JVM shutdown hooks to clean up resources.
     *
     * @param injector the Guice injector containing managed resources
     */
    public static void registerShutdownHooks(Injector injector) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.debug("Shutting down application resources...");
            cleanupResources(injector);
        }));
    }

    /**
     * Cleans up resources that require explicit shutdown.
     * Extend this method to close additional AutoCloseable resources.
     *
     * @param injector the Guice injector containing managed resources
     */
    private static void cleanupResources(Injector injector) {
        try {
            // Pull resources from the injector and close them here to prevent memory leaks
        } catch (Exception e) {
            LOGGER.error("Error during resource cleanup", e);
        }
    }
}
