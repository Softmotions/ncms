package com.softmotions.commons.web;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet provides access to the set of resources
 * stored in jar files in the classpath.
 * Supports automatic content reloading if
 * jar file updated.
 * <p>
 * <p>
 * Servlet parameters in the following format:
 * <pre>
 *      {prefix} => {injar path} [,watch=yes|no]
 * </pre>
 * <p>
 * Example:
 * <pre>
 *  &lt;servlet&gt;
 *      &lt;servlet-name&gt;JarResourcesServlet&lt;/servlet-name&gt;
 *      &lt;servlet-class&gt;com.softmotions.commons.web.JarResourcesServlet&lt;/servlet-class&gt;
 *      &lt;init-param&gt;
 *          &lt;param-name&gt;ncms&lt;/param-name&gt;
 *          &lt;param-value&gt;ncms-engine-qx/ncms, watch=yes&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *  &lt;/servlet&gt;
 * </pre>
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class JarResourcesServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(JarResourcesServlet.class);

    List<MappingSlot> mslots;


    public void init() throws ServletException {
        mslots = new ArrayList<>();
        ServletConfig cfg = getServletConfig();
        Enumeration<String> pnames = cfg.getInitParameterNames();
        while (pnames.hasMoreElements()) {
            String pname = pnames.nextElement();
            try {
                handleJarMapping(pname, cfg.getInitParameter(pname));
            } catch (ServletException e) {
                throw e;
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }

    public void destroy() {
        mslots.clear();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getContent(req, resp, true);
    }

    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getContent(req, resp, false);
    }

    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //todo implement it
        super.doOptions(req, resp);
    }

    void getContent(HttpServletRequest req, HttpServletResponse resp, boolean transfer) throws ServletException, IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        log.info("PathInfo " + req.getPathInfo());
        resp.getWriter().flush();
    }


    MappingSlot findMatchingSlot(HttpServletRequest req) {
        final String path = req.getPathInfo();
        for (final MappingSlot ms : mslots) {
            if (path.startsWith(ms.prefix)) {
                return ms;
            }
        }
        return null;
    }

    void handleJarMapping(String prefix, String spec) throws Exception {
        if (!prefix.isEmpty()) {
            throw new Exception("Empty config param name");
        }
        MappingSlot slot = new MappingSlot(prefix, spec);
        mslots.add(slot);
    }

    private static class MappingSlot {

        final String prefix;

        final String path;

        final Object lock = new Object();

        ClassLoader loader;

        boolean watch;

        private MappingSlot(String prefix, String spec) throws Exception {
            if (prefix.charAt(0) != '/') {
                prefix = "/" + prefix;
            }
            String[] parts = spec.split(",");
            if (parts.length == 0) {
                throw new ServletException(String.format("Invalid configuration param %s: %s", prefix, spec));
            }
            this.path = StringUtils.strip(parts[0].trim(), "/");
            for (int i = 1; i < parts.length; ++i) {
                String p = parts[i];
                String[] pp = p.split("=");
                if (pp.length == 2) {
                    p = pp[0].trim();
                    switch (p) {
                        case "watch":
                            this.watch = BooleanUtils.toBoolean(pp[1]);
                            break;
                    }
                }
            }
            this.prefix = prefix;
        }

        public String toString() {
            return "MappingSlot{" + prefix + " => " + path + '}';
        }

        public int hashCode() {
            return prefix.hashCode();
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            return prefix.equals(obj);
        }
    }


}
