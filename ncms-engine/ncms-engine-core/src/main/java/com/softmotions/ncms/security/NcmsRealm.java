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
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import com.softmotions.web.security.WSRole;
import com.softmotions.web.security.WSUser;
import com.softmotions.web.security.WSUserDatabase;

/**
 * An implementation of the {@link AuthorizingRealm Realm} interface based on {@link WSUserDatabase}
 *
 * @author Motyrev Pavel (legioner.r@gmail.com)
 */
public class NcmsRealm extends AuthorizingRealm {


    private WSUserDatabase database;

    public void setDatabase(WSUserDatabase database) {
        this.database = database;
    }

    public NcmsRealm() {
    }

    public NcmsRealm(WSUserDatabase database) {
        this.database = database;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();
        WSUser user = database.findUser(username);
        if (user == null) {
            throw new UnknownAccountException();
        }
        String password = user.getPassword();
        return new SimpleAuthenticationInfo(username, password, getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Set<String> roleNames = new LinkedHashSet<String>();
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
