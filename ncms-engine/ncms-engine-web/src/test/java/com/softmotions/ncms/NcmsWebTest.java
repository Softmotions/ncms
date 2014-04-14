package com.softmotions.ncms;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.softmotions.ncms.security.XMLWSUserDatabaseJNDIFactory;

import org.eclipse.jetty.plus.jndi.Resource;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Before;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.net.URI;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsWebTest implements NcmsTestServerInitializer {

    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Backend of the test => Starts Ninja
     */
    public NcmsTestServer testServer;

    /**
     * A persistent HttpClient that stores cookies to make requests
     */
    public NcmsTestBrowser testBrowser;

    @Before
    public void startupServerAndBrowser() throws Exception {
        System.setProperty("ninja.mode", "test");
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        testServer = new NcmsTestServer(this);
        testBrowser = new NcmsTestBrowser();
        testServer.getInjector().injectMembers(this);
        afterServerStart();
    }

    /**
     * Something like http://localhost:8080/
     * <p/>
     * Will contain trailing slash!
     *
     * @return
     */
    public String getServerAddress() {
        return testServer.getServerAddress();
    }

    public URI getServerAddressAsUri() {
        return testServer.getServerAddressAsUri();
    }

    @After
    public void shutdownServerAndBrowser() throws Exception {
        try {
            beforeServerShutdown();
        } finally {
            testServer.shutdown();
            testBrowser.shutdown();
        }
    }

    protected void afterServerStart() throws Exception {
    }


    protected void beforeServerShutdown() throws Exception {

    }

    public void initServer(Server server, ServletContextHandler context) {
        bindJNDIResource("WSUserDatabase",
                         new XMLWSUserDatabaseJNDIFactory().setConfig("conf/ncms-test-users.xml"));
    }


    /**
     * Bind JNDI resource into Jetty java:comp/env context
     */
    protected void bindJNDIResource(String name, Object res) {
        checkEnvJNDIContext();
        try {
            new Resource(name, res).bindToENC(name);
        } catch (NamingException e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }

    private void checkEnvJNDIContext() {
        try {
            Context ctx = new InitialContext();
            Context compCtx = (Context) ctx.lookup("java:comp");
            try {
                compCtx.lookup("env");
            } catch (NameNotFoundException e) {
                compCtx.createSubcontext("env");
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }
}
