package com.softmotions.ncms;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import com.google.inject.Injector;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Before;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsWebTest implements NcmsTestServerInitializer {

    /**
     * Backend of the test => Starts Ninja
     */
    public NcmsTestServer ncmsTestServer;

    /**
     * A persistent HttpClient that stores cookies to make requests
     */
    public NcmsTestBrowser ncmsTestBrowser;


    @Before
    public void startupServerAndBrowser() throws Exception {
        System.setProperty("ninja.mode", "test");
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        ncmsTestServer = new NcmsTestServer(this);
        ncmsTestBrowser = new NcmsTestBrowser();
        afterServerStart();
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
    public void shutdownServerAndBrowser() throws Exception {
        try {
            beforeServerShutdown();
        } finally {
            ncmsTestServer.shutdown();
            ncmsTestBrowser.shutdown();
        }
    }

    protected void afterServerStart() throws Exception {
    }


    protected void beforeServerShutdown() throws Exception {

    }

    public void initContext(ServletContextHandler context) {

    }
}
