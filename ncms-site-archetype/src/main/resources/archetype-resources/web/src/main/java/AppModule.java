package ${package};

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.softmotions.weboot.WBConfiguration;

/**
 * Custom application Guice module
 */
public class AppModule extends AbstractModule {

    private final WBConfiguration cfg;

    public AppModule(WBConfiguration cfg) {
        this.cfg = cfg;
    }

    @Override
    protected void configure() {
        // Register your modules here
    }
}
