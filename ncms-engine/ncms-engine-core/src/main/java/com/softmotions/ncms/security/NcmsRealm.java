package com.softmotions.ncms.security;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softmotions.web.security.WSRole;
import com.softmotions.web.security.WSUser;
import com.softmotions.web.security.WSUserDatabase;

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
        this(database, null);
    }

    public NcmsRealm(WSUserDatabase database, @Nullable CredentialsMatcher matcher) {
        this.database = database;
        if (matcher != null) {
            setCredentialsMatcher(matcher);
        } else {
            log.warn("No CredentialsMatcher was set! Fallback to PasswordMatcher.");
        }
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();
        WSUser user = database.findUser(username);
        if (user == null) {
            throw new UnknownAccountException();
        }

        if (getCredentialsMatcher() instanceof NcmsPasswordMatcher) {
            return new SimpleAuthenticationInfo(username, user, getName());
        }

        // fallback to PasswordMatcher
        return new SimpleAuthenticationInfo(username, user.getPassword(), getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Set<String> roleNames = new LinkedHashSet<>();
        String username = getAvailablePrincipal(principals).toString();
        WSUser user = database.findUser(username);
        if (user != null) {
            Iterator<WSRole> roles = user.getRoles();
            while (roles.hasNext()) {
                roleNames.add(roles.next().getName());
            }
        }
        return new SimpleAuthorizationInfo(roleNames);
    }
}
