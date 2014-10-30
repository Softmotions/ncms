package com.softmotions.ncms.update;

import com.softmotions.weboot.lifecycle.Start;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public class UpdateModule extends AbstractModule {
    private static final Logger log = LoggerFactory.getLogger(UpdateModule.class);

    protected void configure() {
        bind(UpdateInitializer.class).asEagerSingleton();

        // default: empty collection of updates
        Multibinder<HotFix> hotfixes = Multibinder.newSetBinder(binder(), HotFix.class);
    }

    public static class UpdateInitializer {

        private final Set<HotFix> hotFixes;

        @Inject
        public UpdateInitializer(Set<HotFix> hotFixes) {
            this.hotFixes = hotFixes;
        }

        @Start(order = Integer.MAX_VALUE)
        public void init() {
            for (HotFix hotFix : hotFixes) {
                try {
                    hotFix.apply();
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }
    }
}
