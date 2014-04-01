package com.softmotions.commons.weboot;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import ninja.lifecycle.Start;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

/**
 * Liquibase Guice integration.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class WBLiquibaseModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(WBLiquibaseModule.class);

    protected void configure() {
        bind(LiquibaseInitializer.class).asEagerSingleton();
    }

    public static class LiquibaseInitializer {

        @Inject
        DataSource ds;

        @Inject
        WBConfiguration cfg;

        @Start(order = 10)
        public void start() {
            XMLConfiguration xcfg = cfg.impl();
            SubnodeConfiguration lbCfg = xcfg.configurationAt("liquibase");
            if (lbCfg == null) {
                log.warn("No <liquibase> configuration found");
                return;
            }
            String changelogResource = lbCfg.getString("[@changelog]");
            if (changelogResource == null) {
                throw new RuntimeException("Missing required attribute 'changelog' in <liquibase> configuration tag");
            }
            log.info("Using changelog: " + changelogResource);

            try (Connection connection = ds.getConnection()) {
                Database database = DatabaseFactory.getInstance()
                        .findCorrectDatabaseImplementation(new JdbcConnection(connection));
                database.setDefaultSchemaName(lbCfg.getString("defaultSchema"));
                Liquibase liquibase =
                        new Liquibase(changelogResource,
                                      new CompositeResourceAccessor(
                                              new ClassLoaderResourceAccessor(),
                                              new FileSystemResourceAccessor(),
                                              new ClassLoaderResourceAccessor(Thread.currentThread()
                                                                                      .getContextClassLoader())
                                      ),
                                      database
                        );

                List<HierarchicalConfiguration> hcList =
                        lbCfg.configurationsAt("liquibase.changelog-parameters.parameter");
                for (final HierarchicalConfiguration hc : hcList) {
                    String name = hc.getString("[@name]");
                    String value = hc.getString("[@value]");
                    if (name != null) {
                        liquibase.setChangeLogParameter(name, value);
                    }
                }

                List<ConfigurationNode> children = lbCfg.getRootNode().getChildren();
                for (ConfigurationNode c : children) {
                    String cn = c.getName();
                    switch (cn) {
                        case "update":
                            String contexts = getSingleStringAttribute(c, "contexts");
                            log.info("Executing Liquibase.Update, contexts=" + contexts);
                            liquibase.update(contexts);
                            break;
                        case "dropAll":
                            log.info("Executing Liqubase.DropAll");
                            liquibase.dropAll();
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                log.error("Failed to initiate WBLiquibaseModule", e);
                throw new RuntimeException(e);
            }
        }
    }

    static String getSingleStringAttribute(ConfigurationNode c, String name) {
        if (c.isAttribute()) return null;
        List<ConfigurationNode> attrs = c.getAttributes(name);
        if (attrs.isEmpty()) return null;
        ConfigurationNode attrNode = attrs.get(0);
        return String.valueOf(attrNode.getValue());
    }
}
