package com.softmotions.ncms.security;

import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.web.security.WSUserDatabase;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsSecurityModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(NcmsSecurityModule.class);

    protected void configure() {
        bind(WSUserDatabase.class).toProvider(WSUserDatabaseProvider.class).asEagerSingleton();
    }

    public static class WSUserDatabaseProvider implements Provider<WSUserDatabase> {

        @Inject
        NcmsConfiguration cfg;

        public WSUserDatabase get() {
            WSUserDatabase usersDb = null;
            String jndiName = cfg.impl().getString("security[@dbJndiName]");
            if (!StringUtils.isBlank(jndiName)) {
                log.info("Locating users database with JNDI name: " + jndiName);
                try {
                    Context ctx = new InitialContext();
                    usersDb = (WSUserDatabase) ctx.lookup(jndiName);
                    usersDb = (WSUserDatabase) ctx.lookup(jndiName);
                    usersDb = (WSUserDatabase) ctx.lookup(jndiName);
                } catch (NamingException e) {
                    log.error("", e);
                    throw new RuntimeException(e);
                }
            }
            if (usersDb == null) {
                throw new RuntimeException("Unable to locate users database, please check the Ncms config");
            }
            log.info("Users database: " + usersDb);
            return usersDb;
        }
    }
}
