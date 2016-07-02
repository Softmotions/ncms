package com.softmotions.ncms.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;


/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
@Singleton
public class BrowserFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(BrowserFilter.class);

    private static final Pattern IE_PATTERN = Pattern.compile("Trident/([0-9]+(\\.[0-9]+)?)");

    private float minTrident;
    private String[] excludePrefixes;
    private String redirectUri;

    public void init(FilterConfig cfg) throws ServletException {
        minTrident = Float.valueOf(cfg.getInitParameter("min-trident"));
        redirectUri = cfg.getInitParameter("redirect-uri");

        String ss = cfg.getInitParameter("exclude-prefixes");
        if (ss != null) {
            excludePrefixes = ss.split(",");
        } else {
            excludePrefixes = ArrayUtils.EMPTY_STRING_ARRAY;
        }

        log.info("Redirect to '{}' for IE Trident less than {}", redirectUri, minTrident);
        log.info("Exclude prefixes: {}", Arrays.asList(excludePrefixes));
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String pi = req.getRequestURI();
        if (StringUtils.isBlank(redirectUri) || redirectUri.equals(pi)) {
            chain.doFilter(request, response);
            return;
        }
        for (final String ep : excludePrefixes) {
            if (pi.startsWith(ep)) {
                chain.doFilter(request, response);
                return;
            }
        }

        String userAgent = req.getHeader("User-Agent");
        Pattern pattern = IE_PATTERN;
        Matcher matcher = (userAgent != null) ? pattern.matcher(userAgent) : null;
        if (matcher == null || !matcher.find()) {
            chain.doFilter(request, response);
            return;
        }

        try {
            if (Float.valueOf(matcher.group(1)) < minTrident) {
                resp.sendRedirect(redirectUri);
                return;
            }
        } catch (Exception ignored) {
        }
        chain.doFilter(request, response);
    }

    public void destroy() {

    }
}
