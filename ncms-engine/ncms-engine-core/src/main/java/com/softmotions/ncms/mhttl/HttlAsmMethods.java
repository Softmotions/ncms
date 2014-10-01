package com.softmotions.ncms.mhttl;

import com.softmotions.commons.cont.KVOptions;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.GeneralDataRS;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.media.MediaRS;

import java.util.Collection;
import java.util.Collections;
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

    public static Object asmAny(String val) {
        return asmHasAttribute(val) ? asm(val) : null;
    }

    public static Object asmAny(Asm asm, String val) {
        if (asm == null) {
            return null;
        }
        return asm.isHasAttribute(val) ? asm(asm, val) : null;
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

    public static boolean pagePdfExists() {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        GeneralDataRS datars = ctx.getInjector().getInstance(GeneralDataRS.class);
        return datars.isPdfExists(ctx.getAsm().getId());
    }
}
