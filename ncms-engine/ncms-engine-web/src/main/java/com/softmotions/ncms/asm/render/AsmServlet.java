package com.softmotions.ncms.asm.render;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Asm handler.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class AsmServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AsmServlet.class);

    @Inject
    Injector injector;

    @Inject
    Provider<AsmRenderer> rendererProvider;

    @Inject
    Provider<AsmResourceResolver> resolverProvider;


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

        //Set charset before calling javax.servlet.ServletResponse.getWriter()
        //Assumed all assemblies generated as utf8 encoded text data.
        //Content-Type can be overriden by assembly renderer.
        resp.setContentType("text/html;charset=UTF-8");

        Writer out = transfer ? resp.getWriter() : new StringWriter();

        String ref = req.getPathInfo();
        if (ref.charAt(0) == '/') {
            ref = ref.substring(1);
        }
        if (ref.isEmpty()) {
            log.warn("Invalid pathInfo: " + req.getPathInfo());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Object asmRef;
        try {
            asmRef = Long.parseLong(ref);
        } catch (NumberFormatException e) {
            asmRef = ref;
        }
        AsmRenderer renderer = rendererProvider.get();
        AsmResourceResolver resolver = resolverProvider.get();
        AsmRendererContext ctx;

        try {
            ctx = new AsmRendererContextImpl(injector,
                                             renderer,
                                             resolver,
                                             req, resp, asmRef);
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
        ctx.push();
        try {
            ctx.render(out);
            if (transfer) {
                out.flush();
            } else {
                resp.setContentLength(((StringWriter) out).getBuffer().length());
                resp.flushBuffer();
            }
        } catch (AsmResourceNotFoundException e) {
            log.error("Resource not found: " + e.getResource() + " assembly: " + ctx.getAsm().getName());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
            ctx.pop();
        }
    }
}
