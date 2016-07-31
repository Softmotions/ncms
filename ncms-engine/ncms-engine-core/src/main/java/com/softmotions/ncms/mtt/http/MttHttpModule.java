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
        filterBinder.addBinding().to(MttVHostsFilter.class);
        filterBinder.addBinding().to(MttParametersFilter.class);
        filterBinder.addBinding().to(MttHeadersFilter.class);
        filterBinder.addBinding().to(MttCookiesFilter.class);

        Multibinder<MttActionHandler> actionBinder = Multibinder.newSetBinder(binder(), MttActionHandler.class);
        actionBinder.addBinding().to(MttRouteAction.class);
        actionBinder.addBinding().to(MttLogAction.class);
        actionBinder.addBinding().to(MttGroupAction.class);
    }
}
