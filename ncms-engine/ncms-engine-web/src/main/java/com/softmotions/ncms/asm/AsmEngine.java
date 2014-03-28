package com.softmotions.ncms.asm;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Assembly engine.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmEngine {

    Collection<String> getAsmNames();

    Asm loadAsm(String asmName);

    Iterator<Asm> findAsms(String query, Object... params);

    Asm findOne(String query, Object... params);

    Asm createAsm(String asmName, Asm parent) throws AsmException;

    void dropAsm(String asmName) throws AsmException;

    AsmAttribute setAsmAttribute(String asmName, String attrName, String attrType,
                                 String attrValue, Map<String, String> attrOptions) throws AsmException;

    void dropAsmAttribute(String asmName, String attrName) throws AsmException;

    void clearAllAsms() throws AsmException;
}
