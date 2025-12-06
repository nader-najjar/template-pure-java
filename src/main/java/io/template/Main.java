package io.template;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.template.bootstrap.injectionmodules.EnvironmentModule;
import io.template.bootstrap.logic.Executor;
import io.template.bootstrap.logic.LifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application entry point.
 * This class is the sole class that must not have associated unit tests - smoke tests are used in their place.
 */
public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Main() { }

    public static void main(String[] args) {
        try {
            Injector injector = Guice.createInjector(
                    new EnvironmentModule()
            );

            LifecycleManager.registerShutdownHooks(injector);

            Executor executor = injector.getInstance(Executor.class);
            executor.execute(args);
        } catch (Exception e) {
            safeCleanup(e);
            System.exit(1);
        }
    }

    private static void safeCleanup(Exception e) {
        LOGGER.error("Technical exception occurred: ", e);
    }
}
