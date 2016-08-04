package com.softmotions.ncms.security;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.collections.map.Flat3Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.softmotions.commons.JVMResources;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.web.AccessControlHDRFilter;
import com.softmotions.web.security.WSRole;
import com.softmotions.web.security.WSUser;
import com.softmotions.web.security.WSUserDatabase;
import com.softmotions.web.security.XMLWSUserDatabase;
import com.softmotions.weboot.WBServletInitializerModule;
import com.softmotions.weboot.WBServletModule;


/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class NcmsSecurityModule extends AbstractModule implements WBServletInitializerModule {

    private static final Logger log = LoggerFactory.getLogger(NcmsSecurityModule.class);

    @Override
    protected void configure() {
        bind(WSUserDatabase.class).toProvider(WSUserDatabaseProvider.class).asEagerSingleton();
        bind(NcmsSecurityRS.class).in(Singleton.class);
        bind(NcmsSecurityContext.class).to(NcmsSecurityContextImpl.class).in(Singleton.class);
    }

    @Override
    public void initServlets(WBServletModule m) {
        NcmsEnvironment env = (NcmsEnvironment) m.getConfiguration();
        String dbJndiName = env.xcfg().getString("security.dbJndiName");
        String dbJVMName = env.xcfg().getString("security.dbJVMName");
        String webAccessControlAllow = env.xcfg().getString("security.web-access-control-allow");
        WSUserDatabase udb = null;
        if (!StringUtils.isBlank(dbJVMName)) {
            udb = JVMResources.getOrFail(dbJVMName);
        }
        if (udb == null && !StringUtils.isBlank(dbJndiName)) {
            udb = locateWSUserDatabase(dbJndiName);
        }
        if (udb != null) {
            List<String> roleNames = new ArrayList<>();
            Iterator<WSRole> roles = udb.getRoles();
            while (roles.hasNext()) {
                WSRole role = roles.next();
                roleNames.add(role.getName());
            }
            log.info("Roles declared in the current servlet context: {}", roleNames);
            m.getWBServletContext().declareRoles(roleNames.toArray(new String[roleNames.size()]));
        }
        if (webAccessControlAllow != null) {
            log.info("Enabled Access-Control-Allow-{Origin|Headers|Methods}={}", webAccessControlAllow);
            Map<String, String> params = new Flat3Map();
            params.put("enabled", "true");
            params.put("headerValue", webAccessControlAllow);
            m.filterAndBind("/*", AccessControlHDRFilter.class, params);
        }
    }

    public static class WSUserDatabaseProvider implements Provider<WSUserDatabase> {

        @Inject
        NcmsEnvironment env;

        @Override
        public WSUserDatabase get() {
            WSUserDatabase usersDb = null;
            String dbJVMName = env.xcfg().getString("security.dbJVMName");
            String jndiName = env.xcfg().getString("security.dbJndiName");

            if (!StringUtils.isBlank(dbJVMName)) {
                log.info("Locating users database with JVM name: {}", dbJVMName);
                // TODO: rewrite
                usersDb = new XMLWSUserDatabase(dbJVMName,
                                                          env.xcfg().getString("security.xml-user-database"),
                                                          true);
                JVMResources.set(dbJVMName, usersDb);
            }
            if (usersDb == null && !StringUtils.isBlank(jndiName)) {
                log.info("Locating users database with JNDI name: {}", jndiName);
                usersDb = locateWSUserDatabase(jndiName);
            }
            if (usersDb == null) {
                throw new RuntimeException("Unable to locate users database, please check the Ncms config");
            }
            log.info("Users database: {}", usersDb);
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

    public static class NcmsSecurityContextImpl implements NcmsSecurityContext {

        final WSUserDatabase database;

        @Inject
        public NcmsSecurityContextImpl(WSUserDatabase database) {
            this.database = database;
        }

        @Override
        public WSUser getWSUser(Principal p) {
            return database.findUser(p.getName());
        }
    }
}
