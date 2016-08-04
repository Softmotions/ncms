package com.softmotions.ncms.security;

import com.softmotions.web.security.WSRole;
import com.softmotions.web.security.WSUser;
import com.softmotions.web.security.WSUserDatabase;
import org.apache.catalina.Wrapper;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An implementation of the {@link AuthorizingRealm Realm} interface based on {@link WSUserDatabase}
 *
 * @author Motyrev Pavel (legioner.r@gmail.com)
 */
public class NcmsRealm extends AuthorizingRealm {

    private static final Logger log = LoggerFactory.getLogger(NcmsRealm.class);

    private WSUserDatabase database;

    public void setDatabase(WSUserDatabase database) {
        this.database = database;
    }

    public NcmsRealm() {
    }

    public NcmsRealm(WSUserDatabase database) {
        this.database = database;
    }

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();
        WSUser user = database.findUser(username);
        String password = user.getPassword();

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(username, password, getName());
        return info;
    }

    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Set<String> roleNames = new LinkedHashSet<String>();

        String username = getAvailablePrincipal(principals).toString();
        WSUser user = database.findUser(username);

        Iterator<WSRole> roles = user.getRoles();
        while (roles.hasNext()) {
            roleNames.add(roles.next().getName());
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roleNames);
        return info;
    }
}
