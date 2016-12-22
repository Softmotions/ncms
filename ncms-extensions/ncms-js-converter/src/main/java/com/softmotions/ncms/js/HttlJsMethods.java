package com.softmotions.ncms.js;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.softmotions.ncms.asm.render.AsmRendererContext;

/**
 * Js compiler - httl integration
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class HttlJsMethods {

    private HttlJsMethods() {
    }

    /**
     * Return HTTP URL to the minified composition of the specified scripts.
     *
     * @param scripts List of script files participated in compilation.
     */
    public static String jsUrl(String[] scripts) {
        return jsUrl(scripts, Collections.emptyMap());
    }

    /**
     * Return HTTP URL to the minified composition of the specified scripts.
     *
     * @param scripts List of script files participated in compilation.
     * @param opts    Scripts compilation options.
     *                `in` - Input ECMA language level, one of: `es3`, `es5`, `es6`. Default: `es5`
     *                `out` - Output ECMA language level, one of: `es3`, `es5`, `es6`. Default: `es5`
     */
    public static String jsUrl(String[] scripts, Map<String, String> opts) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        JsServiceRS jsService = ctx.getInjector().getInstance(JsServiceRS.class);
        Object ncmspage = ctx.get("ncmspage");
        if (ncmspage != null) {
            for (int i = 0; i < scripts.length; ++i) {
                String s = StringUtils.trimToEmpty(scripts[i]);
                if (!s.isEmpty() && s.charAt(0) != '/') {
                    scripts[i] = "/pages/" + ncmspage + '/' + s;
                } else {
                    scripts[i] = s;
                }
            }
        }
        return jsService.createScriptRef(scripts, opts);
    }

    /**
     * Return script tag HTML markup to the minified composition of the specified scripts.
     *
     * @param scripts List of script files participated in compilation.
     *                Every entry must be an absolute media repository path to a script file.
     * @param opts    Scripts compilation options.
     *                `in` - Input ECMA language level, one of: `es3`, `es5`, `es6`. Default: `es5`
     *                `out` - Output ECMA language level, one of: `es3`, `es5`, `es6`. Default: `es5`
     */
    public static String jsScript(String[] scripts, Map<String, String> opts) {
        return "<script type=\"text/javascript\" src=\"" + jsUrl(scripts, opts) + "\"></script>";
    }

    /**
     * Return script tag HTML markup to the minified composition of the specified scripts.
     *
     * @param scripts List of script files participated in compilation.
     *                Every entry must be an absolute media repository path to a script file.
     */
    public static String jsScript(String[] scripts) {
        return jsScript(scripts, Collections.emptyMap());
    }
}
