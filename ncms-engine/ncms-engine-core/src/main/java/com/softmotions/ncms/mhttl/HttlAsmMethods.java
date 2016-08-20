package com.softmotions.ncms.mhttl;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softmotions.commons.cont.KVOptions;
import com.softmotions.commons.string.EscapeHelper;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.mtt.http.MttHttpFilter;
import com.softmotions.web.HttpUtils;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class HttlAsmMethods {

    private static final Logger log = LoggerFactory.getLogger(HttlAsmMethods.class);

    private HttlAsmMethods() {
    }

    public static Asm page() {
        return AsmRendererContext.getSafe().getAsm();
    }

    public static boolean asmHasAttribute(String name) {
        AsmRendererContext rctx = AsmRendererContext.getSafe();
        return rctx.getRenderer().isHasRenderableAsmAttribute(rctx.getAsm(), rctx, name);
    }

    public static Object asmAny(String name) {
        AsmRendererContext rctx = AsmRendererContext.getSafe();
        return rctx.getRenderer().isHasRenderableAsmAttribute(rctx.getAsm(), rctx, name) ? asm(name) : null;
    }

    public static Object asmAny(Asm asm, String name) {
        if (asm == null) {
            return null;
        }
        AsmRendererContext rctx = AsmRendererContext.getSafe();
        return rctx.getRenderer().isHasRenderableAsmAttribute(rctx.getAsm(), rctx, name) ? asm(name) : null;
    }

    public static Object asm(String val) {
        String attrName;
        Map<String, String> opts;
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        int ind = val.indexOf(',');
        if (ind != -1) {
            attrName = val.substring(0, ind);
            opts = (ind < val.length() - 1) ? new KVOptions(val.substring(ind + 1)) : null;
        } else {
            attrName = val;
            opts = null;
        }
        return ctx.renderAttribute(attrName, opts);
    }

    public static Object asm(String attrName, String ok, String ov) {
        return asmIntern(attrName, ok, ov);
    }

    public static Object asm(String attrName,
                             String ok, String ov,
                             String ok1, String ov1) {
        return asmIntern(attrName,
                         ok, ov,
                         ok1, ov1);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    public static Object asm(String attrName,
                             String ok, String ov,
                             String ok1, String ov1,
                             String ok2, String ov2) {
        return asmIntern(attrName,
                         ok, ov,
                         ok1, ov1,
                         ok2, ov2);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    public static Object asm(String attrName,
                             String ok, String ov,
                             String ok1, String ov1,
                             String ok2, String ov2,
                             String ok3, String ov3) {
        return asmIntern(attrName,
                         ok, ov,
                         ok1, ov1,
                         ok2, ov2,
                         ok3, ov3);
    }

    public static Object asm(Asm asm, String attr) {
        return AsmRendererContext.getSafe().renderAttribute(asm, attr, null);
    }

    public static Object asm(Asm asm, String attr, String opts) {
        KVOptions kvopts = new KVOptions();
        kvopts.loadOptions(opts);
        return AsmRendererContext.getSafe().renderAttribute(asm, attr, kvopts);
    }

    public static Object asm(Asm asm, String attr, String ok, String ov) {
        KVOptions opts = new KVOptions();
        opts.put(ok, ov);
        return AsmRendererContext.getSafe().renderAttribute(asm, attr, opts);
    }

    public static Object asm(Asm asm, String attr,
                             String ok, String ov,
                             String ok1, String ov1) {
        KVOptions opts = new KVOptions();
        opts.put(ok, ov);
        opts.put(ok1, ov1);
        return AsmRendererContext.getSafe().renderAttribute(asm, attr, opts);
    }

    public static Object asm(Asm asm, String attr,
                             String ok, String ov,
                             String ok1, String ov1,
                             String ok2, String ov2) {
        KVOptions opts = new KVOptions();
        opts.put(ok, ov);
        opts.put(ok1, ov1);
        opts.put(ok2, ov2);
        return AsmRendererContext.getSafe().renderAttribute(asm, attr, opts);
    }

    private static Object asmIntern(String attrName, String... extraOpts) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        KVOptions opts = null;
        if (extraOpts.length > 0) {
            String ok = null;
            String ov;
            opts = new KVOptions();
            for (int i = 0; i < extraOpts.length; ++i) {
                if (i % 2 == 0) {
                    ok = extraOpts[i];
                } else {
                    ov = extraOpts[i];
                    if (ok != null) {
                        opts.put(ok, ov);
                    }
                    ok = null;
                }
            }
        }
        return ctx.renderAttribute(attrName, opts);
    }

    public static Collection<Asm> asmNavChilds(String type, int skip, int limit) {
        return asmNavChilds(type, Integer.valueOf(skip), Integer.valueOf(limit));
    }

    public static Collection<Asm> asmNavChilds(String type, Number skip, Number limit) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        Asm asm = ctx.getAsm();
        AsmDAO adao = ctx.getInjector().getInstance(AsmDAO.class);
        AsmDAO.PageCriteria crit = adao.newPageCriteria();
        crit.withPublished(true);
        crit.withNavParentId(asm.getId());
        if (type != null) {
            crit.withTypeLike(type);
        }
        if (skip != null) {
            crit.skip(skip.intValue());
        }
        if (limit != null) {
            crit.limit(limit.intValue());
        }
        crit.onAsm().orderBy("ordinal").desc();
        return crit.selectAsAsms();
    }

    public static Collection<Asm> asmPageQuery(Object critObj, int skip, int limit) {
        return asmPageQuery(critObj, Integer.valueOf(skip), Integer.valueOf(limit));
    }

    public static Collection<Asm> asmPageQuery(Object critObj, Number skip, Number limit) {
        //todo check it
        AsmDAO.PageCriteria crit = (AsmDAO.PageCriteria) critObj;
        if (crit == null) {
            return Collections.EMPTY_LIST;
        }
        if (skip != null) {
            crit.skip(skip.intValue());
        }
        if (limit != null) {
            crit.limit(limit.intValue());
        }
        return crit.selectAsAsms();
    }

    ///////////////////////////////////////////////////////////
    //                    A/B Testing                        //
    ///////////////////////////////////////////////////////////

    public static boolean abtA() {
        return abt("a", false);
    }

    public static boolean abtB() {
        return abt("b", false);
    }

    public static boolean abtC() {
        return abt("c", false);
    }

    public static boolean abtD() {
        return abt("d", false);
    }

    public static boolean abt(String name) {
        return abt(name, false);
    }

    /**
     * A/B testing check.
     *
     * @param name A/B mode to test
     * @param def  Default value if A/B test is not passed
     */
    public static boolean abt(String name, boolean def) {
        if (name == null) {
            return def;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        NcmsEnvironment env = ctx.getEnvironment();
        HttpServletRequest req = ctx.getServletRequest();

        String pi = req.getRequestURI();
        boolean isAdmRequest = pi.startsWith(env.getNcmsAdminRoot());
        if (isAdmRequest) { // All A/B modes are ON in admin preview mode
            return true;
        }
        name = name.toLowerCase();
        Collection<Long> rids =
                (Collection<Long>)
                        req.getAttribute(MttHttpFilter.MTT_RIDS_KEY);

        if (rids == null || rids.isEmpty()) {
            Enumeration<String> pnames = req.getParameterNames();
            while (pnames.hasMoreElements()) {
                String pn = pnames.nextElement();
                if (pn.startsWith("_abm_")) {
                    String pv = req.getParameter(pn);
                    if (pv != null) {
                        StringTokenizer st = new StringTokenizer(",");
                        while (st.hasMoreTokens()) {
                            String m = st.nextToken().trim();
                            if (m.equals(name)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Found _abm_ request parameter: {}={}", pn, pv);
                                }
                                return true;
                            }
                        }
                    }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("No mtt actions found, no _abm_ request parameters found");
            }
            return def;
        }
        Long mrid = null;
        Cookie mcookie = null;
        Set<String> marks = null;
        for (Long rid : rids) {
            Set<String> m = (Set<String>) req.getAttribute("_abm_" + rid);
            if (m != null) {
                mrid = rid;
                marks = m;
            }
        }
        if (marks == null) {
            for (Long rid : rids) {
                Cookie cookie = HttpUtils.findCookie(req, "_abm_" + rid);
                if (cookie != null) {
                    mrid = rid;
                    mcookie = cookie;
                }
            }
            if (mcookie != null) {
                String[] split = StringUtils.split(EscapeHelper.decodeURIComponent(mcookie.getValue()), ",");
                marks = new HashSet<>(split.length);
                Collections.addAll(marks, split);
                req.setAttribute("_abm_" + mrid, marks);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("AB Marks: {} matches: {}={}", marks, name,
                      (marks != null && marks.contains(name))
            );
        }
        if (marks == null) {
            return def;
        }
        return marks.contains(name) || def;
    }
}
