package com.softmotions.ncms.asm.render;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.CachedPage;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.web.GenericResponseWrapper;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmRendererContextImpl extends AsmRendererContext {

    final Injector injector;

    final HttpServletRequest req;

    final HttpServletResponse resp;

    final Asm asm;

    final AsmRenderer renderer;

    final AsmResourceLoader loader;

    final ClassLoader classLoader;

    final boolean subcontext;

    final NcmsEnvironment env;

    final NcmsMessages messages;

    final PageService pageService;

    final MediaRepository mediaRepository;

    Locale cachedLocale;

    Map<String, Asm> asmCloneContext;

    Map<String, String[]> dedicatedParams;

    Asm rootAsm;


    private AsmRendererContextImpl(NcmsEnvironment env,
                                   Injector injector,
                                   ClassLoader classLoader,
                                   AsmRenderer renderer,
                                   AsmResourceLoader loader,
                                   NcmsMessages messages,
                                   PageService pageService,
                                   MediaRepository mediaRepository,
                                   HttpServletRequest req, HttpServletResponse resp,
                                   Asm asm) {
        this.env = env;
        this.injector = injector;
        this.renderer = renderer;
        this.loader = loader;
        this.req = req;
        this.resp = resp;
        this.asm = asm;
        this.rootAsm = asm;
        this.classLoader = classLoader;
        this.pageService = pageService;
        this.mediaRepository = mediaRepository;
        this.messages = messages;
        this.subcontext = true;
    }

    @Inject
    public AsmRendererContextImpl(NcmsEnvironment env,
                                  Injector injector,
                                  AsmRenderer renderer,
                                  AsmResourceLoader loader,
                                  NcmsMessages messages,
                                  PageService pageService,
                                  MediaRepository mediaRepository,
                                  @Assisted HttpServletRequest req,
                                  @Assisted HttpServletResponse resp,
                                  @Assisted Object asmRef) throws AsmRenderingException {
        this.env = env;
        this.injector = injector;
        this.renderer = renderer;
        this.loader = loader;
        this.messages = messages;
        this.pageService = pageService;
        this.mediaRepository = mediaRepository;
        this.req = req;
        this.resp = resp;
        this.subcontext = false;
        this.classLoader = Objects.firstNonNull(
                Thread.currentThread().getContextClassLoader(),
                getClass().getClassLoader());
        Asm asm0;
        if (asmRef instanceof Asm) {
            asm0 = (Asm) asmRef;
        } else {
            PageService ps = injector.getInstance(PageService.class);
            CachedPage cp;
            if (asmRef instanceof Number) {
                cp = ps.getCachedPage(((Number) asmRef).longValue(), true);
            } else {
                cp = ps.getCachedPage((String) asmRef, true);
            }
            asm0 = (cp != null) ? cp.getAsm() : null;
            if (asm0 == null) {
                throw new AsmResourceNotFoundException("asm: " + asmRef);
            }
        }
        //Clone the assembly to allow
        //rendering routines be free to change assembly structure and properties
        this.asmCloneContext = new HashMap<>();
        this.asm = asm0.cloneDeep(this.asmCloneContext);
        this.rootAsm = asm;
    }

    public AsmRenderer getRenderer() {
        return renderer;
    }

    public HttpServletRequest getServletRequest() {
        return req;
    }

    public HttpServletResponse getServletResponse() {
        return resp;
    }

    public Injector getInjector() {
        return injector;
    }

    public NcmsEnvironment getEnvironment() {
        return env;
    }

    public Map<String, String[]> getDedicatedRequestParams() {
        if (dedicatedParams != null) {
            return dedicatedParams;
        }
        String prefix = String.format("%d!", asm.getId());
        Map<String, String[]> allparams = req.getParameterMap();
        dedicatedParams = new HashMap<>(allparams.size());

        for (Map.Entry<String, String[]> pe : allparams.entrySet()) {
            String pn = pe.getKey();
            if (!pn.startsWith(prefix)) {
                continue;
            }
            pn = pn.substring(prefix.length());
            if (!pn.isEmpty()) {
                dedicatedParams.put(pn, pe.getValue());
            }
        }
        return dedicatedParams;
    }

    public String getDedicatedParam(String pname) {
        Map<String, String[]> dparams = getDedicatedRequestParams();
        String[] pvals = dparams.get(pname);
        if (pvals == null || pvals.length == 0) {
            return null;
        }
        return pvals[0];
    }

    public Asm getAsm() {
        return asm;
    }

    public Asm getRootAsm() {
        return rootAsm;
    }

    public boolean isSubcontext() {
        return subcontext;
    }

    public AsmRendererContext createSubcontext(String asmName, Writer out) throws AsmResourceNotFoundException {
        PageService ps = injector.getInstance(PageService.class);
        CachedPage cp = ps.getCachedPage(asmName, true);
        Asm nasm = (cp != null) ? cp.getAsm() : null;
        if (nasm == null) {
            throw new AsmResourceNotFoundException("asm: '" + asmName + "'");
        }
        AsmRendererContextImpl nctx =
                new AsmRendererContextImpl(env, injector, classLoader, renderer, loader,
                                           messages, pageService, mediaRepository,
                                           req, new GenericResponseWrapper(resp, out, false),
                                           nasm.cloneDeep(asmCloneContext));
        nctx.asmCloneContext = asmCloneContext;
        nctx.rootAsm = asm;
        nctx.putAll(this);
        return nctx;
    }

    public AsmRendererContext createSubcontext(Asm nasm) throws AsmResourceNotFoundException {
        if (nasm == null) {
            throw new IllegalArgumentException("asm cannot be null");
        }
        if (asm.equals(nasm)) {
            return this;
        }
        AsmRendererContextImpl nctx =
                new AsmRendererContextImpl(env, injector, classLoader, renderer, loader,
                                           messages, pageService, mediaRepository,
                                           req, resp,
                                           nasm.cloneDeep(asmCloneContext));
        nctx.asmCloneContext = asmCloneContext;
        nctx.rootAsm = asm;
        nctx.putAll(this);
        return nctx;
    }


    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public PageService getPageService() {
        return pageService;
    }

    public MediaRepository getMediaRepository() {
        return mediaRepository;
    }

    public Locale getLocale() {
        if (cachedLocale != null) {
            return cachedLocale;
        }
        cachedLocale = messages.getLocale(getServletRequest());
        return cachedLocale;
    }

    public NcmsMessages getMessages() {
        return messages;
    }

    public Object renderAttribute(String attributeName, Map<String, String> opts) {
        return renderer.renderAsmAttribute(this, attributeName, opts);
    }

    public Object renderAttribute(Asm nasm, String attributeName, Map<String, String> opts) {
        if (this.asm.equals(nasm)) {
            return renderAttribute(attributeName, opts);
        }
        Object res = null;
        AsmRendererContext nctx = this.createSubcontext(nasm);
        nctx.push();
        try {
            res = nctx.renderAttribute(attributeName, opts);
        } finally {
            nctx.pop();
        }
        return res;
    }

    public void render() throws AsmRenderingException, IOException {
        renderer.renderAsm(this);
    }

    public AsmResourceLoader getLoader() {
        return loader;
    }
}
