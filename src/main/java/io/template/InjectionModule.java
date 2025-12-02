package io.template;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.template.shared.utilities.HibernateValidatorFactory;
import io.template.shared.utilities.JsonMapperFactory;
import jakarta.validation.Validator;

/**
 * Guice dependency injection module.
 * Configures bindings for the application.
 */
public class InjectionModule extends AbstractModule {

    @Provides
    @Singleton
    JsonMapper provideJsonMapper() {
        return JsonMapperFactory.createStrictMapper();
    }

    @Provides
    @Singleton
    Validator provideValidator() {
        return HibernateValidatorFactory.createValidator();
    }
}
