package ru.nsu;

import ru.nsu.events.EventsRemember;
import ru.nsu.pagepdf.PagePdfRS;
import ru.nsu.pressa.NSUPressaRS;
import ru.nsu.social.NewsTwitterHandler;
import com.softmotions.ncms.update.HotFix;

import com.github.cage.Cage;
import com.github.cage.image.ConstantColorGenerator;
import com.github.cage.token.RandomTokenGenerator;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import java.awt.*;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NSUModule extends AbstractModule {

    protected void configure() {
        bind(NSUPressaRS.class).in(Singleton.class);
        bind(PagePdfRS.class).in(Singleton.class);
        bind(EventsRemember.class).in(Singleton.class);
        bind(NewsTwitterHandler.class).in(Singleton.class);

        initCaptcha();

        initHotFixes();
    }

    protected void initCaptcha() {
        // TODO: create config for captcha settings?
        Cage cage = new Cage(null, null, new ConstantColorGenerator(Color.BLACK), "png", null, new RandomTokenGenerator(null, 6), null);
        bind(Cage.class).toInstance(cage);
    }

    private void initHotFixes() {
        Multibinder<HotFix> hotfixes = Multibinder.newSetBinder(binder(), HotFix.class);

        // add your hotfixes here
        // hotfixes.addBinding().toInstance();
        // hotfixes.addBinding().toProvider();
        // hotfixes.addBinding().to();
    }
}
