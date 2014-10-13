package com.softmotions.ncms.qa;

import com.softmotions.ncms.asm.Asm;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface PageQAModule {

    void checkPage(Asm page, PageQAContext ctx);
}
