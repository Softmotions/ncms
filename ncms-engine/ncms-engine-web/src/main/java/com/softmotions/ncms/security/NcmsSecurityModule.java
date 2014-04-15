package com.softmotions.ncms.security;

import com.softmotions.commons.weboot.WBServletInitializerModule;
import com.softmotions.commons.weboot.WBServletModule;
import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.web.security.SecurityFakeEnvFilter;
import com.softmotions.web.security.WSUserDatabase;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.apache.commons.collections.map.Flat3Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;


/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class NcmsSecurityModule extends AbstractModule implements WBServletInitializerModule {

    private static final Logger log = LoggerFactory.getLogger(NcmsSecurityModule.class);

    protected void configure() {
        bind(WSUserDatabase.class).toProvider(WSUserDatabaseProvider.class).asEagerSingleton();
    }

    public void initServlets(WBServletModule m) {
        NcmsConfiguration cfg = (NcmsConfiguration) m.getConfiguration();
        String dbJndiName = cfg.impl().getString("security[@dbJndiName]");
        String fakeWebUser = cfg.impl().getString("security[@fakeWebUser]");
        if (fakeWebUser != null) {
            log.info("Setup SecurityFakeEnvFilter filter fake web user: " + fakeWebUser);
            if (StringUtils.isBlank(dbJndiName)) {
                throw new RuntimeException("Missing required 'dbJndiName' attribute in the <security> configuration");
            }
            Map<String, String> params = new Flat3Map();
            params.put("dbJndiName", dbJndiName);
            params.put("username", fakeWebUser);
            m.filterAndBind("/*", SecurityFakeEnvFilter.class, params);
        }
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
