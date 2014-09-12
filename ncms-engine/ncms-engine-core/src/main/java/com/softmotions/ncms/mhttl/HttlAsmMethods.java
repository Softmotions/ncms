package com.softmotions.ncms.mhttl;

import com.softmotions.commons.cont.KVOptions;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import java.util.Collection;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class HttlAsmMethods {

    private HttlAsmMethods() {
    }

    public static Asm page() {
        return AsmRendererContext.getSafe().getAsm();
    }

    public static boolean asmHasAttribute(String name) {
        return AsmRendererContext.getSafe().getAsm().isHasAttribute(name);
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

    public static Collection<Asm> asmNews(Integer skip, Integer limit) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        Asm asm = ctx.getAsm();
        AsmDAO adao = ctx.getInjector().getInstance(AsmDAO.class);
        AsmDAO.PageCriteria crit = adao.newPageCriteria();
        crit.withPublished(true);
        crit.withNavParentId(asm.getId());
        crit.withTypeLike("news.page");
        if (skip != null) {
            crit.skip(skip);
        }
        if (limit != null) {
            crit.limit(limit);
        }
        return crit.selectAsAsms();
    }
}
