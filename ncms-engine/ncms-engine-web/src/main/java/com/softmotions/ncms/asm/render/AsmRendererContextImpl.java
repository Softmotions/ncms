package com.softmotions.ncms.asm.render;

import com.softmotions.commons.weboot.mb.MBTinyParams;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;

import com.google.inject.Injector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmRendererContextImpl extends HashMap<String, Object> implements AsmRendererContext {

    final Injector injector;

    final HttpServletRequest req;

    final HttpServletResponse resp;

    final Asm asm;

    final AsmResourceResolver resolver;

    final ClassLoader classLoader;

    Map<String, Asm> asmCloneContext;

    Map<String, String[]> dedicatedParams;


    private AsmRendererContextImpl(Injector injector, ClassLoader classLoader,
                                   AsmResourceResolver resolver,
                                   HttpServletRequest req, HttpServletResponse resp,
                                   Asm asm) {
        this.injector = injector;
        this.resolver = resolver;
        this.req = req;
        this.resp = resp;
        this.asm = asm;
        this.classLoader = classLoader;
    }

    public AsmRendererContextImpl(Injector injector, AsmResourceResolver resolver,
                                  HttpServletRequest req, HttpServletResponse resp,
                                  Object asmRef)
            throws AsmRenderingException {

        this.injector = injector;
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
                               new MBTinyParams()
                                       .param((asmRef instanceof Number) ? "id" : "name",
                                              asmRef)
                );
        if (localAsm == null) {
            throw new AsmRenderingException("Assembly not found with ID/name: " + asmRef);
        }
        //Clone the assembly to allow
        //rendering routines be free to change assembly structure and properties
        this.asmCloneContext = new HashMap<>();
        this.asm = localAsm.cloneDeep(this.asmCloneContext);
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

    public Asm getContextAsm() {
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
                new AsmRendererContextImpl(injector, classLoader, resolver, req, resp, nasm);
        nctx.asmCloneContext = asmCloneContext;
        nctx.putAll(this);
        return nctx;
    }

    public Reader resolveResource(AsmRendererContext ctx, String location) throws IOException {
        return resolver.resolveResource(ctx, location);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
