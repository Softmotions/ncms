package com.softmotions.ncms.asm.render;

import com.softmotions.commons.weboot.mb.MBTinyParams;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class DefaultAsmRendererContext implements AsmAttributeRendererContext {

    final HttpServletRequest req;

    final HttpServletResponse resp;

    final AsmDAO adao;

    final Asm asm;

    final String attributeName;

    Map<String, Asm> asmCloneContext;

    Map<String, String[]> dedicatedParams;

    private DefaultAsmRendererContext(HttpServletRequest req, HttpServletResponse resp,
                                     AsmDAO adao, Asm asm, String attributeName)
            throws AsmRenderingException {
        this.req = req;
        this.resp = resp;
        this.adao = adao;
        this.asm = asm;
        this.attributeName = attributeName;
    }

    public DefaultAsmRendererContext(HttpServletRequest req, HttpServletResponse resp,
                                     AsmDAO adao, long asmId)
            throws AsmRenderingException {
        this(req, resp, adao, asmId, null);
    }

    public DefaultAsmRendererContext(HttpServletRequest req, HttpServletResponse resp,
                                     AsmDAO adao, String asmName)
            throws AsmRenderingException {
        this(req, resp, adao, asmName, null);
    }

    public DefaultAsmRendererContext(HttpServletRequest req, HttpServletResponse resp,
                                     AsmDAO adao, Object asmRef, String attributeName)
            throws AsmRenderingException {

        this.req = req;
        this.resp = resp;
        this.adao = adao;
        this.attributeName = attributeName;
        Asm localAsm =
                adao.selectOne("selectAsmByCriteria",
                               new MBTinyParams()
                                       .param((asmRef instanceof Number) ? "id" : "name",
                                              asmRef)
                );
        if (localAsm == null) {
            throw new AsmRenderingException("Assembly not found with ID/name: " + asmRef);
        }

        //Perform assembly clone in order current thread to be free on changing asm props
        this.asmCloneContext = new HashMap<>();
        this.asm = localAsm.cloneDeep(this.asmCloneContext);
    }

    public HttpServletRequest getServletRequest() {
        return req;
    }

    public HttpServletResponse getServletResponse() {
        return resp;
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

    public String getAttributeName() {
        return attributeName;
    }

    public AsmRendererContext createChildContext(String asmname, String attrname) {
        Asm nasm = adao.selectAsmByName(asmname);
        if (nasm == null) {
            throw new AsmRenderingException("Unknown asm: '" + asmname + "'");
        }
        nasm = nasm.cloneDeep(asmCloneContext);
        return new DefaultAsmRendererContext(req, resp, adao, nasm, attrname);
    }
}
