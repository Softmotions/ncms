package com.softmotions.ncms.asm.render;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.CachedPage;
import com.softmotions.ncms.asm.PageSecurityService;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.media.MediaResource;
import com.softmotions.web.GenericResponseWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * Asm handler.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class AsmFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AsmFilter.class);

    @Inject
    private NcmsEnvironment env;

    @Inject
    private MediaRepository mediaRepository;

    @Inject
    private NcmsMessages messages;

    @Inject
    private PageSecurityService pageSecurity;

    @Inject
    private PageService pageService;

    @Inject
    private AsmRendererContextFactory rendererContextFactory;

    private String[] stripPrefixes;

    private String[] excludePrefixes;


    public void init(FilterConfig cfg) throws ServletException {
        stripPrefixes = null;
        excludePrefixes = null;

        String ss = cfg.getInitParameter("strip-prefixes");
        if (ss != null) {
            ArrayList<String> arr = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(ss, ",");
            while (st.hasMoreTokens()) {
                String sp = st.nextToken().trim();
                if ("/".equals(sp)) {
                    sp = "";
                }
                arr.add(sp);
            }
            stripPrefixes = arr.toArray(new String[arr.size()]);
        } else {
            stripPrefixes = ArrayUtils.EMPTY_STRING_ARRAY;
        }

        ss = cfg.getInitParameter("exclude-prefixes");
        if (ss != null) {
            ArrayList<String> arr = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(ss, ",");
            while (st.hasMoreTokens()) {
                arr.add(st.nextToken().trim());
            }
            excludePrefixes = arr.toArray(new String[arr.size()]);
        } else {
            excludePrefixes = ArrayUtils.EMPTY_STRING_ARRAY;
        }

        log.info("Strip prefixes: " + Arrays.asList(stripPrefixes));
        log.info("Exclude prefixes: " + Arrays.asList(excludePrefixes));
    }

    public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) sreq;
        HttpServletResponse resp = (HttpServletResponse) sresp;
        if (!getContent(req, resp, !"HEAD".equals(req.getMethod()))) {
            chain.doFilter(req, resp);
        }
    }

    public void destroy() {
    }


    @Transactional
    protected boolean getContent(HttpServletRequest req, HttpServletResponse resp, boolean transfer) throws ServletException, IOException {
        String pi = req.getRequestURI();
        for (final String ep : excludePrefixes) {
            if (pi.startsWith(ep)) {
                return false;
            }
        }
        for (final String sp : stripPrefixes) {
            if (pi.startsWith(sp)) {
                pi = pi.substring(sp.length());
                break;
            }
        }
        if (processResources(pi, req, resp)) { //find resources
            return true;
        }
        //Set charset before calling javax.servlet.ServletResponse.getWriter()
        //Assumed all assemblies generated as utf8 encoded text data.
        //Content-Type can be overriden by assembly renderer.
        resp.setContentType("text/html;charset=UTF-8");

        Object asmRef = fetchAsmRef(pi, req);
        if (asmRef == null) {
            //resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        AsmRendererContext ctx;
        HttpServletResponse renderResp = resp;
        StringWriter out = null;
        if (!transfer) {
            out = new StringWriter();
            renderResp = new GenericResponseWrapper(resp, out, false);
        }
        try {
            ctx = rendererContextFactory.createStandalone(req, renderResp, asmRef);
        } catch (AsmResourceNotFoundException e) {
            log.error("Resource not found: " + e.getResource() + " assembly: " + asmRef);
            //resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }

        boolean preview = "1".equals(req.getParameter("preview"));
        Asm asm = ctx.getAsm();
        if (!asm.isPublished()) {
            if (!(preview && pageSecurity.checkAccessAny(asm.getId(), req, "wnd"))) {
                if (asm.getType() != null && asm.getCore() != null) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                return true;
            }
        }

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        //noinspection ObjectEquality
        if (old != ctx.getClassLoader()) {
            Thread.currentThread().setContextClassLoader(ctx.getClassLoader());
        }
        try {
            ctx.render();
            if (!transfer) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                resp.setContentLength(out.getBuffer().length());
            }
        } catch (AsmMissingCoreException e) {
            if (preview) {
                //ignore it
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                throw e;
            }
        } catch (AsmResourceNotFoundException e) {
            log.error("Resource not found: " + e.getResource() + " assembly: " + asm.getName());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
            resp.flushBuffer();
        }
        return true;
    }

    protected Object fetchAsmRef(String pi, HttpServletRequest req) {
        if (pi.length() < 2 || "/index.html".equals(pi)) {
            CachedPage cp = pageService.getIndexPage(req);
            if (cp == null) {
                log.warn("Unable to find index page");
                return null;
            }
            return cp.getAsm();
        }
        pi = pi.substring(1);
        if (pi.endsWith(".html")) {
            pi = pi.substring(0, pi.length() - ".html".length());
        }
        if (pi.length() != 32) { // may be it is a number? (asm ID)
            try {
                return Long.parseLong(pi);
            } catch (NumberFormatException ignored) {
            }
        }
        return pi;
    }

    protected boolean processResources(String pi, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if ("/index.html".equals(pi)) {
            return false;
        }
        XMLConfiguration xcfg = env.xcfg();
        if (!xcfg.getBoolean("asm.site-files-root[@resolveRelativePaths]", true)) {
            return false;
        }
        String siteRoot = xcfg.getString("asm.site-files-root");
        MediaResource mres = mediaRepository.findMediaResource(siteRoot + pi, messages.getLocale(req));
        if (mres == null) {
            return false;
        }
        resp.setContentType(mres.getContentType());
        if (mres.getLength() >= 0L) {
            resp.setContentLength((int) mres.getLength());
        }
        try (InputStream is = mres.openStream()) {
            IOUtils.copyLarge(is, resp.getOutputStream());

        }
        return true;
    }
}
