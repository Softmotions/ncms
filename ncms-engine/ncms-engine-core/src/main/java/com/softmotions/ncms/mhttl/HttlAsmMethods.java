package com.softmotions.ncms.mhttl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softmotions.commons.cont.KVOptions;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.PageCriteria;
import com.softmotions.ncms.asm.render.AsmRendererContext;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public final class HttlAsmMethods {

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

    public static Collection<Asm> asmNavChilds(String type) {
        return asmNavChilds(type, 0, 1000); //todo it is hardcoded limit
    }

    public static Collection<Asm> asmNavChilds(String type, int skip, int limit) {
        return asmNavChilds(type, Integer.valueOf(skip), Integer.valueOf(limit));
    }

    private static final AtomicReference<AsmDAO> ASM_DAO_REF = new AtomicReference<>();

    private static AsmDAO getAsmDAO(AsmRendererContext ctx) {
        AsmDAO adao = ASM_DAO_REF.get();
        if (adao != null) {
            return adao;
        }
        return ASM_DAO_REF.updateAndGet(
                asmDAO ->
                        asmDAO != null ? asmDAO : ctx.getInjector().getInstance(AsmDAO.class));
    }

    public static Collection<Asm> asmNavChilds(String type, Number skip, Number limit) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        Asm asm = ctx.getAsm();
        AsmDAO adao = getAsmDAO(ctx);

        PageCriteria crit = adao.newPageCriteria();
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
        if (!(critObj instanceof PageCriteria)) {
            return Collections.EMPTY_LIST;
        }
        PageCriteria crit = (PageCriteria) critObj;
        if (skip != null) {
            crit.skip(skip.intValue());
        }
        if (limit != null) {
            crit.limit(limit.intValue());
        }
        return crit.selectAsAsms();
    }


    public static String link(Asm asm) {
        return (asm != null) ? AsmRendererContext.getSafe().getPageService().resolvePageLink(asm.getName()) : null;
    }

    public static String link(String alias) {
        return (alias != null) ? AsmRendererContext.getSafe().getPageService().resolvePageLink(alias) : null;
    }

    public static String resolve(String link) {
        return (link != null) ? AsmRendererContext.getSafe().getPageService().resolvePageLink(link) : null;
    }

    public static String link(RichRef ref) {
        if (ref == null) {
            return null;
        }
        return ref.getLink();
    }

    public static String linkHtml(Object ref) {
        return linkHtml(ref, null);
    }

    public static String linkHtml(Object ref, Map<String, ?> attrs) {
        if (ref == null) {
            return null;
        }
        //noinspection ChainOfInstanceofChecks
        if (ref instanceof Tree) {
            return ((Tree) ref).toHtmlLink(attrs);
        }
        if (ref instanceof String) {
            ref = new RichRef((String) ref, AsmRendererContext.getSafe().getPageService());
        }
        if (!(ref instanceof RichRef)) {
            return null;
        }
        return ((RichRef) ref).toHtmlLink(attrs);
    }

    ///////////////////////////////////////////////////////////
    //                       Casts                           //
    ///////////////////////////////////////////////////////////

    public static RichRef asRichRef(Object v) {
        return (v instanceof RichRef) ? (RichRef) v : null;
    }

    public static Tree asTree(Object v) {
        return (v instanceof Tree) ? (Tree) v : null;
    }

    public static Table asTable(Object v) {
        return (v instanceof Table) ? (Table) v : null;
    }

    public static Image asImage(Object v) {
        return (v instanceof Image) ? (Image) v : null;
    }

    ///////////////////////////////////////////////////////////
    //                  Social systems support               //
    ///////////////////////////////////////////////////////////

    public static String ogmeta() {
        return ogmeta(null);
    }

    public static String ogmeta(Map<String, String> params) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        NcmsEnvironment env = ctx.getEnvironment();
        HttpServletRequest req = ctx.getServletRequest();
        Asm asm = ctx.getAsm();

        if (params == null) {
            params = new HashMap<>();
        }

        params.put("url", req.getRequestURL().toString());
        params.put("site_name", req.getServerName());
        params.put("locale", ctx.getLocale().toString());

        if (!params.containsKey("title")) {
            params.put("title", asm.getHname());
        }

        if (!params.containsKey("type")) {
            params.put("type", "article");
        }

        if (params.containsKey("image")) {
            String imgLink = params.get("image");
            if (asmHasAttribute(imgLink)) {
                Object imgRef = asm(imgLink);
                if (imgRef instanceof Image) {
                    Image img = (Image) imgRef;
                    imgLink = img.getLink();
                }
            }
            params.put("image", env.getAbsoluteLink(req, imgLink));
        }

        StringBuilder ret = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!e.getKey().isEmpty()) {
                ret.append("<meta property=\"og:").append(e.getKey())
                   .append("\" content=\"").append(e.getValue()).append("\"/>\n");
            }
        }

        return ret.toString();
    }
}
