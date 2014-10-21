package ru.nsu;

import ru.nsu.events.EventsRemember;
import ru.nsu.pagepdf.PagePdfRS;
import ru.nsu.pressa.NSUPressaRS;
import ru.nsu.social.NewsTwitterHandler;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NSUModule extends AbstractModule {

    protected void configure() {
        bind(NSUPressaRS.class).in(Singleton.class);
        bind(PagePdfRS.class).in(Singleton.class);
        bind(EventsRemember.class).in(Singleton.class);
        bind(NewsTwitterHandler.class).in(Singleton.class);
    }
}
