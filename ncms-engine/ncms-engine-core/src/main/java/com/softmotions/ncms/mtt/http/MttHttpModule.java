package com.softmotions.ncms.mtt.http;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MttHttpModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<MttFilterHandler> filterBinder = Multibinder.newSetBinder(binder(), MttFilterHandler.class);
        filterBinder.addBinding().to(MttVHostsFilterHandler.class);
        filterBinder.addBinding().to(MttParametersFilterHandler.class);

        Multibinder<MttActionHandler> actionBinder = Multibinder.newSetBinder(binder(), MttActionHandler.class);
        actionBinder.addBinding().to(MttRouteActionHandler.class);
        actionBinder.addBinding().to(MttLogActionHandler.class);
        actionBinder.addBinding().to(MttGroupActionHandler.class);
    }
}
