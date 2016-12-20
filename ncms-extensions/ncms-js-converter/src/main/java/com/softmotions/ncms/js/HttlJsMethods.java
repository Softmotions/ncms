package com.softmotions.ncms.js;

import java.util.Collections;
import java.util.Map;

import com.softmotions.ncms.asm.render.AsmRendererContext;

/**
 * Js compiler - httl integration
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class HttlJsMethods {

    private HttlJsMethods() {
    }

    /**
     * Return HTTP URL to the minified composition of the specified scripts.
     *
     * @param scripts List of script files participated in compilation.
     *                Every entry must be an absolute media repository path to a script file.
     */
    public static String jsUrl(String[] scripts) {
        return jsUrl(scripts, Collections.emptyMap());
    }

    /**
     * Return HTTP URL to the minified composition of the specified scripts.
     *
     * @param scripts List of script files participated in compilation.
     *                Every entry must be an absolute media repository path to a script file.
     * @param opts    Scripts compilation options.
     *                `in` - Input ECMA language level, one of: `es3`, `es5`, `es6`. Default: `es5`
     *                `out` - Output ECMA language level, one of: `es3`, `es5`, `es6`. Default: `es5`
     */
    public static String jsUrl(String[] scripts, Map<String, String> opts) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        JsServiceRS jsService = ctx.getInjector().getInstance(JsServiceRS.class);
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
