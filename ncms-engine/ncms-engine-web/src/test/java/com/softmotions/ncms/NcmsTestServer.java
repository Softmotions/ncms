package com.softmotions.ncms;

import com.google.inject.Injector;

import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.Configuration;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsTestServer {

    private final int port;
    private final URI serverUri;
    private final NcmsJetty container;


    public NcmsTestServer(NcmsTestServerInitializer initializer) {
        this.port = findAvailablePort(8000, 10000);
        serverUri = createServerUri();

        container = new NcmsJetty(initializer);
        container.setPort(this.port);
        container.setServerUri(serverUri);
        container.start();
    }

    public Injector getInjector() {
        return container.getInjector();
    }

    public String getServerAddress() {
        return serverUri.toString();
    }

    public URI getServerAddressAsUri() {
        return serverUri;
    }

    private URI createServerUri() {
        try {
            return new URIBuilder().setScheme("http").setHost("localhost")
                    .setPort(port).build();
        } catch (URISyntaxException e) {
            // should not be able to happen...
            return null;
        }
    }

    public void shutdown() {
        container.shutdown();
    }

    private static int findAvailablePort(int min, int max) {
        for (int port = min; port < max; port++) {
            try {
                new ServerSocket(port).close();
                return port;
            } catch (IOException e) {
                // Must already be taken
            }
        }
        throw new IllegalStateException(
                "Could not find available port in range " + min + " to " + max);
    }


    public static class NcmsJetty {

        static final int DEFAULT_PORT = 8080;

        int port;

        URI serverUri;

        Server server;

        ServletContextHandler context;

        String contextPath;

        NcmsServletListener servletListener;

        NcmsTestServerInitializer initializer;

        public NcmsJetty(NcmsTestServerInitializer initializer) {
            //some sensible defaults
            port = DEFAULT_PORT;
            serverUri = URI.create("http://localhost:" + port);
            this.initializer = initializer;
        }

        public Injector getInjector() {
            return servletListener.getInjector();
        }

        public NcmsJetty setPort(int port) {
            this.port = port;
            return this;
        }

        public NcmsJetty setServerUri(URI serverUri) {
            this.serverUri = serverUri;
            return this;
        }

        public NcmsJetty setContextPath(String contextPath) {
            this.contextPath = contextPath;
            return this;
        }

        public void start() {
            server = new Server(port);
            try {

                Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
                classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration",
                                   "org.eclipse.jetty.plus.webapp.EnvConfiguration",
                                   "org.eclipse.jetty.plus.webapp.PlusConfiguration");

                ServerConnector http = new ServerConnector(server);
                server.addConnector(http);
                context = new ServletContextHandler(server, contextPath);

                ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
                context.setSecurityHandler(securityHandler);

                //todo init!!!
                servletListener = new NcmsServletListener();
                context.addEventListener(servletListener);

                initializer.initServer(server, context);
                context.addServlet(DefaultServlet.class, "/");

                server.start();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        public void shutdown() {
            try {
                server.stop();
                server.destroy();
                context.stop();
                context.destroy();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String getServerAddress() {
            return serverUri.toString() + "/";
        }

        public URI getServerAddressAsUri() {
            return serverUri;
        }
    }

}
