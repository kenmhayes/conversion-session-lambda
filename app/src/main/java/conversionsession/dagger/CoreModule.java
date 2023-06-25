package conversionsession.dagger;

import com.fasterxml.jackson.databind.ObjectMapper;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for providing various commonly used Java utilities
 */
@Module
public abstract class CoreModule {
    @Provides
    public static ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }
}
