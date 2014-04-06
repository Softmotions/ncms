package com.softmotions.ncms.asm.render;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;

import com.google.inject.Injector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
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

    final AsmResourceResolver resolver;

    final ClassLoader classLoader;

    Map<String, Asm> asmCloneContext;

    Map<String, String[]> dedicatedParams;


    private AsmRendererContextImpl(Injector injector,
                                   ClassLoader classLoader,
                                   AsmRenderer renderer,
                                   AsmResourceResolver resolver,
                                   HttpServletRequest req, HttpServletResponse resp,
                                   Asm asm) {
        this.injector = injector;
        this.renderer = renderer;
        this.resolver = resolver;
        this.req = req;
        this.resp = resp;
        this.asm = asm;
        this.classLoader = classLoader;
    }

    public AsmRendererContextImpl(Injector injector,
                                  AsmRenderer renderer,
                                  AsmResourceResolver resolver,
                                  HttpServletRequest req, HttpServletResponse resp,
                                  Object asmRef)
            throws AsmRenderingException, AsmResourceNotFoundException {

        this.injector = injector;
        this.renderer = renderer;
        this.resolver = resolver;
        this.req = req;
        this.resp = resp;
        if (Thread.currentThread().getContextClassLoader() != null) {
            this.classLoader = Thread.currentThread().getContextClassLoader();
        } else {
            this.classLoader = getClass().getClassLoader();
        }

        AsmDAO adao = injector.getInstance(AsmDAO.class);
        Asm localAsm =
                adao.selectOne("selectAsmByCriteria",
                               ((asmRef instanceof Number) ? "id" : "name"), asmRef);
        if (localAsm == null) {
            throw new AsmResourceNotFoundException("asm: " + asmRef);
        }
        //Clone the assembly to allow
        //rendering routines be free to change assembly structure and properties
        this.asmCloneContext = new HashMap<>();
        this.asm = localAsm.cloneDeep(this.asmCloneContext);
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

    public AsmRendererContext createSubcontext(String asmname) {
        AsmDAO adao = injector.getInstance(AsmDAO.class);
        Asm nasm = adao.selectAsmByName(asmname);
        if (nasm == null) {
            throw new AsmRenderingException("Unknown asm: '" + asmname + "'");
        }
        nasm = nasm.cloneDeep(asmCloneContext);
        AsmRendererContextImpl nctx =
                new AsmRendererContextImpl(injector, classLoader, renderer, resolver,
                                           req, resp, nasm);
        nctx.asmCloneContext = asmCloneContext;
        nctx.putAll(this);
        return nctx;
    }

    public Reader openResourceReader(String location) throws IOException {
        return resolver.openResourceReader(this, location);
    }

    public InputStream openResourceInputStream(String location) throws IOException {
        return resolver.openResourceInputStream(this, location);
    }

    public List<String> listResources(String directory, String suffix) throws IOException {
        return resolver.listResources(this, directory, suffix);
    }

    public boolean isResourceExists(String location) {
        return resolver.isResourceExists(this, location);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Locale getLocale() {
        //todo
        return Locale.getDefault();
    }

    public String renderAttribute(String attributeName, Map<String, String> opts) {
        return renderer.renderAsmAttribute(this, attributeName, opts);
    }

    public void render(Writer out) throws AsmRenderingException, IOException {
        renderer.renderAsm(this, out);
    }
}
