package com.softmotions.ncms.mtt.http;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class MttHttpModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<MttFilterHandler> filterBinder = Multibinder.newSetBinder(binder(), MttFilterHandler.class);
        filterBinder.addBinding().to(MttVHostsFilter.class);
        filterBinder.addBinding().to(MttParametersFilter.class);
        filterBinder.addBinding().to(MttHeadersFilter.class);
        filterBinder.addBinding().to(MttCookiesFilter.class);
        filterBinder.addBinding().to(MttUserAgentFilter.class);
        filterBinder.addBinding().to(MttPageFilter.class);
        filterBinder.addBinding().to(MttResourceFilter.class);

        Multibinder<MttActionHandler> actionBinder = Multibinder.newSetBinder(binder(), MttActionHandler.class);
        actionBinder.addBinding().to(MttRouteAction.class);
        actionBinder.addBinding().to(MttLogAction.class);
        actionBinder.addBinding().to(MttGroupAction.class);
        actionBinder.addBinding().to(MttCookieAction.class);
        actionBinder.addBinding().to(MttRequestParametersAction.class);
        actionBinder.addBinding().to(MttCompositeAction.class);
        actionBinder.addBinding().to(MttABMarksAction.class);
        actionBinder.addBinding().to(MttRememberOriginAction.class);
    }
}
