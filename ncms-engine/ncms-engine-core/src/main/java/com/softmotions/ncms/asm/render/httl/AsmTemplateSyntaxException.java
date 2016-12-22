package com.softmotions.ncms.asm.render.httl;

import javax.annotation.Nonnull;

/**
 * Template syntax error.
 * Used in {{@link AsmTemplateEngineHttlAdapter#checkTemplateSyntax(String)}}
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class AsmTemplateSyntaxException extends RuntimeException {

    public AsmTemplateSyntaxException(@Nonnull String message) {
        super(message);
    }
}
