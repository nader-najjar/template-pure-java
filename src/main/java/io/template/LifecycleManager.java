package io.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages application lifecycle using the idempotency-based paradigm.
 * Shutdown hooks ONLY signal termination - cleanup happens in main thread.
 * See: temp-docs/lifecycle-management-paradigm.md
 */
public final class LifecycleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleManager.class);
    private static final long SHUTDOWN_GRACE_PERIOD_MS = 10000; // 10 seconds

    private static volatile boolean shutdownRequested = false;

    private LifecycleManager() { }

    /**
     * Registers JVM shutdown hook that signals termination request.
     * Does NOT cleanup resources - that must happen in the main thread.
     */
    public static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutdown signal received, requesting graceful shutdown...");

            // ONLY set flag - don't cleanup resources!
            shutdownRequested = true;

            // Wait briefly for main thread to cleanup gracefully
            try {
                Thread.sleep(SHUTDOWN_GRACE_PERIOD_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            LOGGER.info("Shutdown hook exiting, JVM will terminate");
            // JVM terminates after this hook completes
        }));
    }

    /**
     * Checks if shutdown has been requested.
     *
     * @return true if shutdown was requested, false otherwise
     */
    public static boolean isShutdownRequested() {
        return shutdownRequested;
    }
}
