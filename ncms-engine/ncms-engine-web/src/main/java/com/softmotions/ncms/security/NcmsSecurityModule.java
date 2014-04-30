package com.softmotions.ncms.security;

import com.softmotions.commons.weboot.WBServletInitializerModule;
import com.softmotions.commons.weboot.WBServletModule;
import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.web.AccessControlHDRFilter;
import com.softmotions.web.security.SecurityFakeEnvFilter;
import com.softmotions.web.security.WSRole;
import com.softmotions.web.security.WSUserDatabase;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.apache.commons.collections.map.Flat3Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class NcmsSecurityModule extends AbstractModule implements WBServletInitializerModule {

    private static final Logger log = LoggerFactory.getLogger(NcmsSecurityModule.class);

    protected void configure() {
        bind(WSUserDatabase.class).toProvider(WSUserDatabaseProvider.class).asEagerSingleton();
        bind(NcmsSecurityRS.class).in(Singleton.class);
    }

    public void initServlets(WBServletModule m) {
        NcmsConfiguration cfg = (NcmsConfiguration) m.getConfiguration();
        String dbJndiName = cfg.impl().getString("security[@dbJndiName]");
        String webFakeUser = cfg.impl().getString("security.web-fakeuser");
        String webAccessControlAllow = cfg.impl().getString("security.web-access-control-allow");

        if (webFakeUser != null) {
            log.info("Setup SecurityFakeEnvFilter filter fake web user: " + webFakeUser);
            if (StringUtils.isBlank(dbJndiName)) {
                throw new RuntimeException("Missing required 'dbJndiName' attribute in the <security> configuration");
            }
            Map<String, String> params = new Flat3Map();
            params.put("dbJndiName", dbJndiName);
            params.put("username", webFakeUser);
            m.filterAndBind("/*", SecurityFakeEnvFilter.class, params);
        }

        if (dbJndiName != null) {
            WSUserDatabase udb = locateWSUserDatabase(dbJndiName);
            List<String> roleNames = new ArrayList<>();
            Iterator<WSRole> roles = udb.getRoles();
            while (roles.hasNext()) {
                WSRole role = roles.next();
                roleNames.add(role.getName());
            }
            log.info("Roles declared in the current servlet context: " + roleNames);
            m.getWBServletContext().declareRoles(roleNames.toArray(new String[roleNames.size()]));
        }

        if (webAccessControlAllow != null) {
            log.info("Enabled Access-Control-Allow-{Origin|Headers|Methods}=" + webAccessControlAllow);
            Map<String, String> params = new Flat3Map();
            params.put("enabled", "true");
            params.put("headerValue", webAccessControlAllow);
            m.filterAndBind("/*", AccessControlHDRFilter.class, params);
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
                usersDb = locateWSUserDatabase(jndiName);
            }
            if (usersDb == null) {
                throw new RuntimeException("Unable to locate users database, please check the Ncms config");
            }
            log.info("Users database: " + usersDb);
            return usersDb;
        }
    }

    private static WSUserDatabase locateWSUserDatabase(String jndiName) {
        Context ctx = null;
        try {
            ctx = new InitialContext();
            return (WSUserDatabase) ctx.lookup(jndiName);
        } catch (NamingException e) {
            log.error("", e);
            throw new RuntimeException(e);
        } finally {
            //noinspection EmptyCatchBlock
            try {
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException e) {
            }
        }
    }
}
