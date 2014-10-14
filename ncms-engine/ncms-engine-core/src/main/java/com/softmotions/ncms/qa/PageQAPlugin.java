package com.softmotions.ncms.qa;

import com.softmotions.ncms.asm.Asm;

/**
   * @author Adamansky Anton (adamansky@gmail.com)
   */
 public interface PageQAPlugin {

     void checkPage(Asm page, PageQAContext ctx) throws Exception;
 }
