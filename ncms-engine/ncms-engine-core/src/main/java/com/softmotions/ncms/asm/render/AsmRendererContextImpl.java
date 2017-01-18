package com.softmotions.ncms.asm.render;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.CachedPage;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.web.GenericResponseWrapper;
import com.softmotions.weboot.i18n.I18n;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@NotThreadSafe
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

    final I18n i18n;

    final PageService pageService;

    final MediaRepository mediaRepository;

    AsmRendererContext parent;

    Locale cachedLocale;

    Map<String, Asm> asmCloneContext;

    Map<String, String[]> dedicatedParams;


    private AsmRendererContextImpl(AsmRendererContextImpl parent,
                                   HttpServletResponse resp,
                                   Asm asm) {
        super(parent.userData);
        this.parent = parent;
        this.req = parent.req;
        this.resp = resp;
        this.env = parent.env;
        this.injector = parent.injector;
        this.renderer = parent.renderer;
        this.loader = parent.loader;
        this.classLoader = parent.classLoader;
        this.pageService = parent.pageService;
        this.mediaRepository = parent.mediaRepository;
        this.i18n = parent.i18n;
        this.subcontext = true;
        this.asm = asm;
        this.asmCloneContext = parent.asmCloneContext;
        this.putAll(parent);
    }

    @Inject
    public AsmRendererContextImpl(NcmsEnvironment env,
                                  Injector injector,
                                  AsmRenderer renderer,
                                  AsmResourceLoader loader,
                                  I18n i18n,
                                  PageService pageService,
                                  MediaRepository mediaRepository,
                                  @Assisted HttpServletRequest req,
                                  @Assisted HttpServletResponse resp,
                                  @Assisted Object asmRef) throws AsmRenderingException {
        this.env = env;
        this.injector = injector;
        this.renderer = renderer;
        this.loader = loader;
        this.i18n = i18n;
        this.pageService = pageService;
        this.mediaRepository = mediaRepository;
        this.req = req;
        this.resp = resp;
        this.subcontext = false;
        this.classLoader = MoreObjects.firstNonNull(
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

        //Set basic content parameters
        this.put("ncmsroot", env.getAppRoot());
        this.put("pageroot", env.getAppRoot() + mediaRepository.getPageLocalFolderPath(this.asm.getId()));
        this.put("ncmspage", this.asm.getId());
    }

    @Override
    public AsmRendererContext getParent() {
        return parent;
    }

    @Override
    public AsmRenderer getRenderer() {
        return renderer;
    }

    @Override
    public HttpServletRequest getServletRequest() {
        return req;
    }

    @Override
    public HttpServletResponse getServletResponse() {
        return resp;
    }

    @Override
    public Injector getInjector() {
        return injector;
    }

    @Override
    public NcmsEnvironment getEnvironment() {
        return env;
    }

    @Override
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

    @Override
    public String getDedicatedParam(String pname) {
        Map<String, String[]> dparams = getDedicatedRequestParams();
        String[] pvals = dparams.get(pname);
        if (pvals == null || pvals.length == 0) {
            return null;
        }
        return pvals[0];
    }

    @Nonnull
    @Override
    public Asm getAsm() {
        return asm;
    }

    @Override
    public boolean isSubcontext() {
        return subcontext;
    }

    @Override
    public AsmRendererContext createSubcontext(String asmName, Writer out) throws AsmResourceNotFoundException {
        PageService ps = injector.getInstance(PageService.class);
        CachedPage cp = ps.getCachedPage(asmName, true);
        Asm nasm = (cp != null) ? cp.getAsm() : null;
        if (nasm == null) {
            throw new AsmResourceNotFoundException("asm: '" + asmName + "'");
        }
        return new AsmRendererContextImpl(this,
                                          new GenericResponseWrapper(resp, out, false),
                                          nasm.cloneDeep(asmCloneContext));
    }

    public AsmRendererContext createSubcontext(Asm nasm) throws AsmResourceNotFoundException {
        if (nasm == null) {
            throw new IllegalArgumentException("nasm cannot be null");
        }
        if (asm.equals(nasm)) {
            return this;
        }
        return new AsmRendererContextImpl(this,
                                          resp,
                                          nasm.cloneDeep(asmCloneContext));
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public PageService getPageService() {
        return pageService;
    }

    @Override
    public MediaRepository getMediaRepository() {
        return mediaRepository;
    }

    @Override
    public Locale getLocale() {
        if (cachedLocale != null) {
            return cachedLocale;
        }
        cachedLocale = i18n.getLocale(getServletRequest());
        return cachedLocale;
    }

    @Override
    public I18n getI18n() {
        return i18n;
    }

    @Override
    public Object renderAttribute(String attributeName, Map<String, String> opts) {
        return renderer.renderAsmAttribute(this, attributeName, opts);
    }

    @Override
    public Object renderAttribute(Asm nasm, String attributeName, Map<String, String> opts) {
        if (this.asm.equals(nasm)) {
            return renderAttribute(attributeName, opts);
        }
        Object res;
        AsmRendererContext nctx = this.createSubcontext(nasm);
        nctx.push();
        try {
            res = nctx.renderAttribute(attributeName, opts);
        } finally {
            nctx.pop();
        }
        return res;
    }

    @Override
    public void render(Writer writer) throws AsmRenderingException, IOException {
        renderer.renderAsm(this, writer);
    }

    @Override
    public AsmResourceLoader getLoader() {
        return loader;
    }
}
