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
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Asm handler.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class AsmServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AsmServlet.class);

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


    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getContent(req, resp, true);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getContent(req, resp, true);
    }

    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getContent(req, resp, false);
    }

    @Transactional
    protected void getContent(HttpServletRequest req, HttpServletResponse resp, boolean transfer) throws ServletException, IOException {

        if (processResources(req, resp)) { //find resources
            return;
        }
        //Set charset before calling javax.servlet.ServletResponse.getWriter()
        //Assumed all assemblies generated as utf8 encoded text data.
        //Content-Type can be overriden by assembly renderer.
        resp.setContentType("text/html;charset=UTF-8");

        Object asmRef = fetchAsmRef(req);
        if (asmRef == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
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
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        boolean preview = "1".equals(req.getParameter("preview"));
        Asm asm = ctx.getAsm();
        if (!asm.isPublished()) {
            if (!(preview && pageSecurity.checkAccessAny(asm.getId(), req, "wnd"))) {
                if (asm.getType() != null && asm.getCore() != null) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                return;
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
    }

    protected Object fetchAsmRef(HttpServletRequest req) {
        String pi = req.getPathInfo();
        if (pi == null || pi.length() < 2 || "/index.html".equals(pi)) {
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

    protected boolean processResources(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pi = req.getPathInfo();
        if (pi == null || "/index.html".equals(pi)) {
            return false;
        }
        XMLConfiguration xcfg = env.xcfg();
        if (!xcfg.getBoolean("site-files-root[@resolveRelativePaths]", true)) {
            return false;
        }
        String siteRoot = xcfg.getString("site-files-root");
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
