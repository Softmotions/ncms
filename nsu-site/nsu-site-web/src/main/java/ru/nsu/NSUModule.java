package ru.nsu;

import ru.nsu.pressa.NSUPressaRS;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NSUModule extends AbstractModule {

    protected void configure() {
        bind(NSUPressaRS.class).in(Singleton.class);
    }
}
