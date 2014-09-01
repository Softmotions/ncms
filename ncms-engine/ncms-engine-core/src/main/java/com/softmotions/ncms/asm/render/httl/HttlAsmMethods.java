package com.softmotions.ncms.asm.render.httl;

import com.softmotions.commons.cont.KVOptions;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.render.AsmRendererContext;

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

    public static String link(Asm asm) {
        return AsmRendererContext.getSafe().getCfg().getAsmLink(asm.getName());
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
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        //noinspection ObjectEquality
        if (ctx.getAsm() != asm) {
            ctx = ctx.createSubcontext(asm);
        }
        return ctx.renderAttribute(attr, null);
    }

    public static Object asm(Asm asm, String attr, String opts) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        //noinspection ObjectEquality
        if (ctx.getAsm() != asm) {
            ctx = ctx.createSubcontext(asm);
        }
        KVOptions kvopts = new KVOptions();
        kvopts.loadOptions(opts);
        return ctx.renderAttribute(attr, kvopts);
    }

    public static Object asm(Asm asm, String attr, String ok, String ov) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        //noinspection ObjectEquality
        if (ctx.getAsm() != asm) {
            ctx = ctx.createSubcontext(asm);
        }
        KVOptions opts = new KVOptions();
        opts.put(ok, ov);
        return ctx.renderAttribute(attr, opts);
    }

    public static Object asm(Asm asm, String attr,
                             String ok, String ov,
                             String ok1, String ov1) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        //noinspection ObjectEquality
        if (ctx.getAsm() != asm) {
            ctx = ctx.createSubcontext(asm);
        }
        KVOptions opts = new KVOptions();
        opts.put(ok, ov);
        opts.put(ok1, ov1);
        return ctx.renderAttribute(attr, opts);
    }

    public static Object asm(Asm asm, String attr,
                             String ok, String ov,
                             String ok1, String ov1,
                             String ok2, String ov2) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        //noinspection ObjectEquality
        if (ctx.getAsm() != asm) {
            ctx = ctx.createSubcontext(asm);
        }
        KVOptions opts = new KVOptions();
        opts.put(ok, ov);
        opts.put(ok1, ov1);
        opts.put(ok2, ov2);
        return ctx.renderAttribute(attr, opts);
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
}
