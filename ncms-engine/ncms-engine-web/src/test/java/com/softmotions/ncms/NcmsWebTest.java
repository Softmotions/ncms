package com.softmotions.ncms;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ninja.utils.NinjaTestBrowser;

import com.google.inject.Injector;

import org.junit.After;
import org.junit.Before;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsWebTest {

    /**
     * Backend of the test => Starts Ninja
     */
    public NcmsTestServer ncmsTestServer;

    /**
     * A persistent HttpClient that stores cookies to make requests
     */
    public NinjaTestBrowser ninjaTestBrowser;


    @Before
    public void startupServerAndBrowser() {
        System.setProperty("ninja.mode", "test");
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        ncmsTestServer = new NcmsTestServer();
        ninjaTestBrowser = new NinjaTestBrowser();
    }

    public Injector getInjector() {
        return ncmsTestServer.getInjector();
    }

    /**
     * Something like http://localhost:8080/
     * <p>
     * Will contain trailing slash!
     *
     * @return
     */
    public String getServerAddress() {
        return ncmsTestServer.getServerAddress();
    }

    public URI getServerAddressAsUri() {
        return ncmsTestServer.getServerAddressAsUri();
    }

    @After
    public void shutdownServerAndBrowser() {
        ncmsTestServer.shutdown();
        ninjaTestBrowser.shutdown();
    }
}
