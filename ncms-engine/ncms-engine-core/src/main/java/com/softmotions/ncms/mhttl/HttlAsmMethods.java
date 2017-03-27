package com.softmotions.ncms.mhttl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.softmotions.commons.cont.KVOptions;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.CachedPage;
import com.softmotions.ncms.asm.PageCriteria;
import com.softmotions.ncms.asm.render.AsmRendererContext;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@SuppressWarnings("unchecked")
public final class HttlAsmMethods {

    private HttlAsmMethods() {
    }

    public static Asm page() {
        return AsmRendererContext.getSafe().getAsm();
    }

    @Nullable
    public static Object asmAnyTTN(String name) {
        AsmRendererContext rctx = AsmRendererContext.getSafe();
        return asmAnyTTN(rctx.getAsm(), name);
    }

    @Nullable
    public static Object asmAnyTTN(Asm asm, String name) {
        AsmRendererContext rctx = AsmRendererContext.getSafe();
        Object v = asmAny(asm, name);
        if (v instanceof String) {
            v = StringUtils.trimToNull((String) v);
        }
        rctx.put("__asmAnyTTN", v);
        return v;
    }

    public static boolean asmHasAnyTTN(String name) {
        return asmAnyTTN(name) != null;
    }

    public static boolean asmHasAnyTTN(Asm asm, String name) {
        return asmAnyTTN(asm, name) != null;
    }

    @Nullable
    public static Object asmCachedTTN() {
        AsmRendererContext rctx = AsmRendererContext.getSafe();
        return rctx.get("__asmAnyTTN");
    }

    public static boolean asmHasAttribute(String name) {
        AsmRendererContext rctx = AsmRendererContext.getSafe();
        return rctx.getRenderer().isHasRenderableAsmAttribute(rctx.getAsm(), rctx, name);
    }

    @Nullable
    public static Object asmAny(String name) {
        AsmRendererContext rctx = AsmRendererContext.getSafe();
        return rctx.getRenderer().isHasRenderableAsmAttribute(rctx.getAsm(), rctx, name) ? asm(name) : null;
    }

    @Nullable
    public static Object asmAny(Asm asm, String name) {
        if (asm == null) {
            return null;
        }
        AsmRendererContext rctx = AsmRendererContext.getSafe();
        return rctx.getRenderer().isHasRenderableAsmAttribute(asm, rctx, name) ? asm(asm, name) : null;
    }

    @Nullable
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

    @Nullable
    public static Object asm(String attrName, String ok, String ov) {
        return asmIntern(attrName, ok, ov);
    }

    @Nullable
    public static Object asm(String attrName,
                             String ok, String ov,
                             String ok1, String ov1) {
        return asmIntern(attrName,
                         ok, ov,
                         ok1, ov1);
    }

    @Nullable
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

    @Nullable
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

    @Nullable
    public static Object asm(Asm asm, String attr) {
        return AsmRendererContext.getSafe().renderAttribute(asm, attr, null);
    }

    @Nullable
    public static Object asm(Asm asm, String attr, String opts) {
        KVOptions kvopts = new KVOptions();
        kvopts.loadOptions(opts);
        return AsmRendererContext.getSafe().renderAttribute(asm, attr, kvopts);
    }

    @Nullable
    public static Object asm(Asm asm, String attr, String ok, String ov) {
        KVOptions opts = new KVOptions();
        opts.put(ok, ov);
        return AsmRendererContext.getSafe().renderAttribute(asm, attr, opts);
    }

    @Nullable
    public static Object asm(Asm asm, String attr,
                             String ok, String ov,
                             String ok1, String ov1) {
        KVOptions opts = new KVOptions();
        opts.put(ok, ov);
        opts.put(ok1, ov1);
        return AsmRendererContext.getSafe().renderAttribute(asm, attr, opts);
    }

    @Nullable
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

    @Nullable
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

    ///////////////////////////////////////////////////////////
    //                      Queries                          //
    ///////////////////////////////////////////////////////////

    private static final AtomicReference<AsmDAO> ASM_DAO_REF = new AtomicReference<>();

    public static Collection<Asm> asmParentNavChilds() {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        Asm asm = ctx.getAsm();
        Long parentId = asm.getNavParentId();
        if (parentId == null) {
            return Collections.emptyList();
        }
        CachedPage cp = ctx.getPageService().getCachedPage(parentId, true);
        if (cp == null) {
            return Collections.emptyList();
        }
        return asmNavChilds(ctx, cp.getAsm(), null, true, null, null);
    }

    private static AsmDAO getAsmDAO(AsmRendererContext ctx) {
        AsmDAO adao = ASM_DAO_REF.get();
        if (adao != null) {
            return adao;
        }
        return ASM_DAO_REF.updateAndGet(
                asmDAO ->
                        asmDAO != null ? asmDAO : ctx.getInjector().getInstance(AsmDAO.class));
    }

    public static Collection<Asm> asmNavChilds() {
        return asmNavChilds(null, true);
    }

    public static Collection<Asm> asmNavChildsAll() {
        return asmNavChilds(null, false);
    }

    public static Collection<Asm> asmNavChilds(@Nullable String type) {
        return asmNavChilds(type, true);
    }

    public static Collection<Asm> asmNavChildsAll(@Nullable String type) {
        return asmNavChilds(type, false);
    }

    public static Collection<Asm> asmNavChilds(@Nullable String type, boolean allPublished) {
        return asmNavChilds(type, allPublished, 0, 1000); //todo it is hardcoded limit
    }

    public static Collection<Asm> asmNavChilds(@Nullable String type, int skip, int limit) {
        return asmNavChilds(type, true, skip, limit);
    }

    public static Collection<Asm> asmNavChildsAll(@Nullable String type, int skip, int limit) {
        return asmNavChilds(type, false, skip, limit);
    }

    public static Collection<Asm> asmNavChilds(@Nullable String type,
                                               boolean allPublished,
                                               int skip,
                                               int limit) {
        return asmNavChilds(type, allPublished, Integer.valueOf(skip), Integer.valueOf(limit));
    }

    public static Collection<Asm> asmNavChilds(@Nullable String type,
                                               boolean allPublished,
                                               @Nullable Number skip,
                                               @Nullable Number limit) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        return asmNavChilds(ctx, ctx.getAsm(), type, allPublished, skip, limit);
    }

    public static Collection<Asm> asmNavChilds(AsmRendererContext ctx, Asm asm,
                                               @Nullable String type,
                                               boolean allPublished,
                                               @Nullable Number skip,
                                               @Nullable Number limit) {
        AsmDAO adao = getAsmDAO(ctx);
        PageCriteria crit = adao.newPageCriteria();
        boolean preview = ctx.getPageService().getPageSecurityService().isPreviewPageRequest(ctx.getServletRequest());
        if (!preview && allPublished) {
            crit.withPublished(true);
        }
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

    public static PageCriteria asmNavChildsPageCriteria() {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        AsmDAO adao = getAsmDAO(ctx);
        PageCriteria crit = adao.newPageCriteria();
        crit.withPublished(true);
        crit.withNavParentId(ctx.getAsm().getId());
        crit.onAsm().orderBy("ordinal").desc();
        return crit;
    }

    public static Collection<Asm> asmPageQuery(Object critObj) {
        return asmPageQuery(critObj, null, null);
    }

    public static Collection<Asm> asmPageQuery(Object critObj, int skip, int limit) {
        return asmPageQuery(critObj, Integer.valueOf(skip), Integer.valueOf(limit));
    }

    public static Collection<Asm> asmPageQuery(Object critObj,
                                               @Nullable Number skip,
                                               @Nullable Number limit) {
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

    ///////////////////////////////////////////////////////////
    //                      Links                            //
    ///////////////////////////////////////////////////////////

    @Nullable
    public static String link(Asm asm) {
        return (asm != null) ? AsmRendererContext.getSafe().getPageService().resolvePageLink(asm.getName()) : null;
    }

    @Nullable
    public static String link(String alias) {
        return (alias != null) ? AsmRendererContext.getSafe().getPageService().resolvePageLink(alias) : null;
    }

    @Nullable
    public static String resolve(String link) {
        return (link != null) ? AsmRendererContext.getSafe().getPageService().resolvePageLink(link) : null;
    }

    @Nullable
    public static String link(RichRef ref) {
        if (ref == null) {
            return null;
        }
        return ref.getLink();
    }

    @Nullable
    public static String linkHtml(Object ref) {
        return linkHtml(ref, null);
    }

    @Nullable
    public static String linkHtml(Object ref,
                                  @Nullable Map<String, ?> attrs) {
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

    @Nullable
    public static RichRef asRichRef(Object v) {
        return (v instanceof RichRef) ? (RichRef) v : null;
    }

    @Nullable
    public static Tree asTree(Object v) {
        return (v instanceof Tree) ? (Tree) v : null;
    }

    @Nullable
    public static Table asTable(Object v) {
        return (v instanceof Table) ? (Table) v : null;
    }

    @Nullable
    public static Image asImage(Object v) {
        return (v instanceof Image) ? (Image) v : null;
    }

    ///////////////////////////////////////////////////////////
    //                  Social systems support               //
    ///////////////////////////////////////////////////////////

    public static String ogmeta() {
        return ogmeta(null);
    }

    public static String ogmeta(@Nullable Map<String, String> params) {
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
