package com.softmotions.ncms;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface NcmsTestServerInitializer {

    void initServer(Server server, ServletContextHandler context);

}
