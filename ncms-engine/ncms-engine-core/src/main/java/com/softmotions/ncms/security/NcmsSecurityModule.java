package com.softmotions.ncms.security;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.map.Flat3Map;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;
import com.softmotions.commons.JVMResources;
import com.softmotions.commons.io.Loader;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.web.AccessControlHDRFilter;
import com.softmotions.web.security.WSRole;
import com.softmotions.web.security.WSUser;
import com.softmotions.web.security.WSUserDatabase;
import com.softmotions.web.security.XMLWSUserDatabase;
import com.softmotions.weboot.WBServletInitializerModule;
import com.softmotions.weboot.WBServletModule;
import com.softmotions.weboot.i18n.I18n;


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
            HierarchicalConfiguration<ImmutableNode> xcfg = env.xcfg();
            String dbJVMName = xcfg.getString("security.dbJVMName");
            String jndiName = xcfg.getString("security.dbJndiName");

            if (!StringUtils.isBlank(dbJVMName)) {
                log.info("Locating users database with JVM name: {}", dbJVMName);
                // XMLDB
                String xmldb = xcfg.getString("security.xml-user-database");
                if (xmldb != null) {
                    String placeTo = xcfg.getString("security.xml-user-database[@placeTo]", null);
                    if (placeTo != null) {
                        File placeToFile = new File(placeTo);
                        if (placeToFile.exists()) {
                            xmldb = placeToFile.getAbsolutePath();
                        } else {
                            File placeToParent = placeToFile.getParentFile();
                            if (placeToParent != null) {
                                placeToParent.mkdirs();
                            }
                            URL url = Loader.getResourceAsUrl(xmldb, getClass());
                            if (url == null) {
                                throw new ProvisionException("Unable to find xml-user-database file/resource: " + xmldb);
                            }
                            try {
                                FileUtils.copyInputStreamToFile(url.openStream(), placeToFile);
                                try {
                                    Files.setPosixFilePermissions(placeToFile.toPath(),
                                                                  PosixFilePermissions.fromString("rw-------"));
                                } catch (UnsupportedOperationException ignored) {
                                }
                            } catch (IOException e) {
                                throw new ProvisionException("Failed to init xml-user-database file: " + placeToFile, e);
                            }
                            xmldb = placeToFile.getAbsolutePath();
                        }
                    }
                    log.info("XML users database locations: {}", xmldb);
                    usersDb = new XMLWSUserDatabase(dbJVMName, xmldb, true);
                    JVMResources.set(dbJVMName, usersDb);
                } else {
                    usersDb = JVMResources.getOrFail(dbJVMName);
                }
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

        final I18n i18n;

        @Inject
        public NcmsSecurityContextImpl(WSUserDatabase database, I18n i18n) {
            this.database = database;
            this.i18n = i18n;
        }

        @Override
        public WSUser getWSUser(Principal p) throws ShiroException {
            return getWSUser(p, null);
        }

        @Override
        public WSUser getWSUser(Principal p, Locale locale) throws ShiroException {
            if (p == null) {
                throw new UnauthenticatedException(i18n.get("ncms.access.notAuthenticated", locale));
            }
            WSUser user = database.findUser(p.getName());
            if (user == null) {
                throw new UnknownAccountException(i18n.get("ncms.access.notFound", locale));
            }
            return user;
        }

        @Override
        public WSUser getWSUser(HttpServletRequest req) throws ShiroException {
            return getWSUser(req.getUserPrincipal(), i18n.getLocale(req));
        }
    }
}
