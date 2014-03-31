package com.softmotions.ncms.db;

import com.softmotions.ncms.NcmsWebTest;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.dao.AsmDAO;

import com.google.inject.Injector;

import org.apache.ibatis.exceptions.PersistenceException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NcmsModelAsmTest extends NcmsWebTest {

    private static final Logger log = LoggerFactory.getLogger(NcmsModelAsmTest.class);

    @Test
    public void testInsertAssembly() throws Exception {
        Injector injector = getInjector();
        AsmDAO adao = injector.getInstance(AsmDAO.class);
        Asm asm = new Asm();
        asm.setName("foo");
        Assert.assertEquals(1, adao.insertAsm(asm));

        List<Asm> allAsms = adao.getAllAsms();
        Assert.assertFalse(allAsms.isEmpty());

        Asm asm2 = allAsms.get(0);
        Assert.assertFalse(asm == asm2);
        Assert.assertEquals(asm.getId(), asm2.getId());
        Assert.assertEquals(asm.getName(), asm2.getName());
        Assert.assertEquals(asm.getDescription(), asm2.getDescription());

        boolean hasException = false;
        try {
            adao.insertAsm(asm);
        } catch (Exception e) {
            hasException = true;
            Assert.assertTrue(e instanceof PersistenceException);
            PersistenceException pe = (PersistenceException) e;
        }
        Assert.assertTrue(hasException);
    }
}
