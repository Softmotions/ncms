package com.softmotions.ncms.asm.render;

import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.media.MediaResource;
import com.softmotions.web.GenericResponseWrapper;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
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
    private Injector injector;

    @Inject
    private Provider<AsmRenderer> rendererProvider;

    @Inject
    private Provider<AsmResourceLoader> loaderProvider;

    @Inject
    private MediaRepository mediaRepository;

    @Inject
    private NcmsConfiguration cfg;

    @Inject
    private NcmsMessages messages;


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

        AsmRenderer renderer = rendererProvider.get();
        AsmResourceLoader loader = loaderProvider.get();
        AsmRendererContext ctx;
        HttpServletResponse renderResp = resp;
        StringWriter out = null;
        if (!transfer) {
            out = new StringWriter();
            renderResp = new GenericResponseWrapper(resp, out, false);
        }
        try {
            ctx = new AsmRendererContextImpl(cfg,
                                             injector,
                                             renderer,
                                             loader,
                                             req, renderResp, asmRef);
        } catch (AsmResourceNotFoundException e) {
            log.error("Resource not found: " + e.getResource() + " assembly: " + asmRef);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
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
        } catch (AsmResourceNotFoundException e) {
            log.error("Resource not found: " + e.getResource() + " assembly: " + ctx.getAsm().getName());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
            resp.flushBuffer();
        }
    }

    protected Object fetchAsmRef(HttpServletRequest req) {
        String asmRefParam = req.getPathInfo();
        if (asmRefParam == null || asmRefParam.length() < 2) {
            log.warn("Invalid pathInfo: " + req.getPathInfo());
            return null;
        }
        asmRefParam = asmRefParam.substring(1);
        if (asmRefParam.endsWith(".html")) {
            asmRefParam = asmRefParam.substring(0, asmRefParam.length() - ".html".length());
        }
        if (asmRefParam.length() != 32) { // may be it is a number? (asm ID)
            try {
                return Integer.parseInt(asmRefParam);
            } catch (NumberFormatException ignored) {
            }
        }
        return asmRefParam;
    }

    protected boolean processResources(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pi = req.getPathInfo();
        if (pi == null) {
            return false;
        }
        XMLConfiguration xcfg = cfg.impl();
        if (!xcfg.getBoolean("site-root[@resolveRelativePaths]", true)) {
            return false;
        }
        String siteRoot = xcfg.getString("site-root");
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
