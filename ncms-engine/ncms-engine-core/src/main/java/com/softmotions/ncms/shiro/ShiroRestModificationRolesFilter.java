package com.softmotions.ncms.shiro;

import java.io.IOException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * Same as {@link RolesAuthorizationFilter}
 * but skips role checking for POST, PUT, DELETE rest method.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class ShiroRestModificationRolesFilter extends RolesAuthorizationFilter {

    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws IOException {
        String m = WebUtils.toHttp(request).getMethod().toUpperCase();
        if ("PUT".equals(m) || "POST".equals(m) || "DELETE".equals(m)) {
            return super.isAccessAllowed(request, response, mappedValue);
        } else {
            return true;
        }
    }
}
