package com.softmotions.ncms.asm.render;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.StringTokenizer;
import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.shiro.web.util.WebUtils;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.CachedPage;
import com.softmotions.ncms.asm.IndexPage;
import com.softmotions.ncms.asm.PageSecurityService;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.media.MediaResource;
import com.softmotions.web.GenericResponseWrapper;
import com.softmotions.web.HttpUtils;
import com.softmotions.weboot.i18n.I18n;

/**
 * Asm handler.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */

@Singleton
public class AsmFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AsmFilter.class);

    private final NcmsEnvironment env;

    private final MediaRepository mediaRepository;

    private final I18n i18n;

    private final PageSecurityService pageSecurity;

    private final PageService pageService;

    private final AsmRendererContextFactory rendererContextFactory;

    private boolean resolveRelativePaths;

    private String siteFilesRoot;

    private String[] stripPrefixes;

    private String[] excludePrefixes;


    @Inject
    public AsmFilter(NcmsEnvironment env,
                     MediaRepository mediaRepository,
                     I18n i18n,
                     PageSecurityService pageSecurity,
                     PageService pageService,
                     AsmRendererContextFactory rendererContextFactory) {
        this.env = env;
        this.mediaRepository = mediaRepository;
        this.i18n = i18n;
        this.pageSecurity = pageSecurity;
        this.pageService = pageService;
        this.rendererContextFactory = rendererContextFactory;
    }

    @Override
    public void init(FilterConfig cfg) throws ServletException {
        this.stripPrefixes = null;
        this.excludePrefixes = null;
        this.resolveRelativePaths = env.xcfg().getBoolean("asm.site-files-root[@resolveRelativePaths]", true);
        this.siteFilesRoot = env.xcfg().getString("asm.site-files-root", "/site");

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
        log.info("Strip prefixes: {}", Arrays.asList(stripPrefixes));
        log.info("Exclude prefixes: {}", Arrays.asList(excludePrefixes));
    }

    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) sreq;
        HttpServletResponse resp = (HttpServletResponse) sresp;
        if (!getContent(req, resp, !"HEAD".equals(req.getMethod()))) {
            chain.doFilter(req, resp);
        }
    }

    @Override
    public void destroy() {
    }


    @Transactional
    protected boolean getContent(HttpServletRequest req,
                                 HttpServletResponse resp,
                                 boolean transfer)
            throws ServletException, IOException, AsmRenderingException {
        String pi = HttpUtils.stripJsessionId(req.getRequestURI());
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
        // Find site resources
        if (processResources(pi, req, resp) || handleCoreUrls(pi, req, resp)) {
            return true;
        }

        i18n.initRequestI18N(req, resp);
        Object asmRef = fetchAsmRef(pi, req);
        if (asmRef == null) {
            return false;
        }

        //Set charset before calling javax.servlet.ServletResponse.getWriter()
        //Assumed all assemblies generated as utf8 encoded text data.
        //Content-Type can be overridden by assembly renderer.
        resp.setContentType("text/html;charset=UTF-8");
        resp.setBufferSize(65536);

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
            log.info("NOT FOUND: {}", e.getResource());
            return false;
        }

        boolean preview = pageSecurity.isPreviewPageRequest(req);
        Asm asm = ctx.getAsm();
        if (!asm.isPublished()) {
            boolean canAccess = (req.getUserPrincipal() != null) && pageSecurity.checkAccessAny(asm.getId(), req, "wnd");
            if (!canAccess) {
                if (!preview) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
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
            ctx.render(null);
            if (!transfer) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                resp.setContentLength(out.getBuffer().length());
            }
            resp.flushBuffer();
        } catch (AsmMissingCoreException e) {
            if (preview) {
                //ignore it
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                throw e;
            }
        } catch (AsmResourceNotFoundException e) {
            log.error("Resource not found: {} assembly: {}", e.getResource(), asm.getName());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException | AsmRenderingException e) {
            log.error("", e);
            throw e;
        } catch (Throwable e) {
            log.error("", e);
            throw new AsmRenderingException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
        return true;
    }


    private boolean handleCoreUrls(String pi,
                                   HttpServletRequest req,
                                   HttpServletResponse resp) throws IOException {
        // Handle request for robots.txt
        if ("/robots.txt".equals(pi)) {
            handleRobots(req, resp);
            return true;
        }
        // Handle request for favicon.ico
        if ("/favicon.ico".equals(pi)) {
            handleFavicon(req, resp);
            return true;
        }
        if ("/login".equals(pi)) {
            handleLogin(req, resp);
            return true;
        }
        return false;
    }

    @Nullable
    protected Object fetchAsmRef(String pi, HttpServletRequest req) {
        if (pi.length() < 2 || "/index.html".equals(pi)) {
            CachedPage cp = pageService.getIndexPage(req, true);
            if (cp == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to find index page");
                }
                return null;
            }
            return cp.getAsm();
        }
        pi = pi.substring(1);
        if (pi.endsWith(".html")) {
            pi = pi.substring(0, pi.length() - ".html".length());
        }
        if (pi.length() != 32 && !pi.isEmpty() && Character.isDigit(pi.charAt(0))) { // may be it is a number? (asm ID)
            try {
                return Long.parseLong(pi);
            } catch (NumberFormatException ignored) {
            }
        }
        return pi;
    }

    protected boolean processResources(String location,
                                       HttpServletRequest req,
                                       HttpServletResponse resp) throws IOException {
        if ("/index.html".equals(location)) {
            return false;
        }
        if (!resolveRelativePaths) {
            return false;
        }
        String l = siteFilesRoot + location;
        Locale locale = i18n.getLocale(req);
        MediaResource mres = mediaRepository.findMediaResource(l, locale);
        if (mres == null) {
            if (location.startsWith("/pages/")) {
                l = location;
                mres = mediaRepository.findMediaResource(l, locale);
            }
            if (mres == null) {
                return false;
            }
        }
        if (!mediaRepository.isAllowedToResponse(mres, req)) {
            // Template are forbidden to be served as static files
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return true;
        }

        resp.setContentType(mres.getContentType());

        // Response with raw resource
        if (mres.getLength() >= 0L) {
            resp.setContentLength((int) mres.getLength());
        }
        try (InputStream is = mres.openStream()) {
            IOUtils.copyLarge(is, resp.getOutputStream());
        }
        resp.flushBuffer();
        return true;
    }

    /**
     * Find and inject in response robots.txt option of mainpage attribute as plain text
     */
    private void handleRobots(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        IndexPage ip = pageService.getIndexPage(req, true);
        String robots;
        if (ip == null || (robots = ip.getRobotsConfig()) == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(robots);
    }

    /**
     * Find and return in response favicon.ico option of mainpage attribute as image
     */
    private void handleFavicon(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        IndexPage ip = pageService.getIndexPage(req, true);
        String favicon;
        if (ip == null || (favicon = ip.getFaviconBase64()) == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("image/png");
        IOUtils.write(Base64.getDecoder().decode(favicon), resp.getOutputStream());
    }

    /**
     * Show nCMS login form
     */
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("X-Softmotions-Login", "true");
        resp.setContentType("text/html;charset=UTF-8");
        WebUtils.getAndClearSavedRequest(req);
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        InputStream is = getClass().getResourceAsStream("/com/softmotions/ncms/login.html");
        if (is == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            IOUtils.copyLarge(is, resp.getOutputStream());
        } finally {
            is.close();
        }
    }
}
