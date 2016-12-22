package com.softmotions.ncms.security;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softmotions.ncms.NcmsEnvironment;

/**
 * Incoming requests guard filter.
 * <p>
 * Ensures serving of admin zone (/adm) in accordance
 * with a set of security restrictions.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class NcmsGuardFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(NcmsGuardFilter.class);

    private URL adminOnUrl;

    private String adminRoot;

    private boolean redirect;

    public NcmsGuardFilter(NcmsEnvironment env) {
        adminRoot = env.getNcmsAdminRoot();
        String adminOn = env.xcfg().getString("admin-zone-on", null);
        redirect = env.xcfg().getBoolean("admin-zone-on[@redirect]", false);
        if (!StringUtils.isEmpty(adminOn)) {
            try {
                adminOnUrl = new URL(adminOn);
            } catch (MalformedURLException e) {
                log.error("", e);
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if (adminOnUrl == null) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest req = (HttpServletRequest) request;
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            pathInfo = req.getRequestURI().substring(req.getContextPath().length());
        }
        boolean isAdmRequest = pathInfo.startsWith(adminRoot);
        if (!isAdmRequest && pathInfo.startsWith("/rs/adm/")) {
            isAdmRequest = true;
        }
        if (!isAdmRequest) {
            chain.doFilter(request, response);
            return;
        }
        if (req.getServerName().equals(adminOnUrl.getHost())) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletResponse resp = (HttpServletResponse) response;
        if (redirect && "GET".equals(req.getMethod())) {
            String redirectUrl = adminOnUrl.toString() + pathInfo;
            if (req.getQueryString() != null) {
                redirectUrl = redirectUrl + '?' + req.getQueryString();
            }
            log.info("Redirecting to the admin zone: {}", redirectUrl);
            resp.sendRedirect(redirectUrl);
        } else {
            log.warn("Access the admin zone is prohibited, since " +
                     "it does not match '/admin-zone-on' config url: {}. " +
                     "Requested url: {}", adminOnUrl, req.getRequestURL());
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override
    public void destroy() {

    }
}
