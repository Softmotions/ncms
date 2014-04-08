package com.softmotions.ncms.asm.renderer1;

import com.softmotions.ncms.HttpTestResponse;
import com.softmotions.ncms.NcmsWebTest;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmCore;
import com.softmotions.ncms.asm.AsmDAO;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test basics of assembly rendering.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmRenderer1Test extends NcmsWebTest {

    private static final Logger log = LoggerFactory.getLogger(AsmRenderer1Test.class);

    protected void afterServerStart() throws Exception {
        AsmDAO adao = getInjector().getInstance(AsmDAO.class);

        //testRendererBasic
        Asm asm = new Asm("asm1",
                          new AsmCore("com/softmotions/ncms/asm/renderer1/core1.txt", "Core1"));
        asm.addAttribute(new AsmAttribute("asm1_attr1", "a15e2f74f8724be9947acc586fd15a84"));
        asm.addAttribute(new AsmAttribute("asm1_attr2", "53496feab7f34f488185edacc70a4739"));
        asm.addAttribute(new AsmAttribute("asm1_attr3", "<asm1_attr3>"));
        adao.asmInsert(asm);

        //testCoreNotFound
        asm = new Asm("asmCoreNotFound",
                      new AsmCore("com/softmotions/ncms/asm/renderer1/coreNotFound.txt", "coreNotFound"));
        adao.asmInsert(asm);


        //Assembly which includes other assembly
        asm = new Asm("asmInc",
                      new AsmCore("com/softmotions/ncms/asm/renderer1/coreAsmInc.httl", "AsmIncCore"));
        asm.addAttribute(new AsmAttribute("asm1_inc", "asmref", "asm1"));
        adao.asmInsert(asm);
    }

    @Test
    public void testRendererBasic() throws Exception {
        HttpTestResponse resp = ncmsTestBrowser.makeGET(getServerAddress() + "/ncms/asm/asm1");
        assertEquals(200, resp.statusCode);
        assertEquals("UTF-8", resp.charset);
        String respStr = resp.toString();
        assertTrue(respStr.contains("f67c7ec829b84e9da79c420f09e04994"));
        assertTrue(respStr.contains("asm1_attr1=a15e2f74f8724be9947acc586fd15a84"));
        assertTrue(respStr.contains("asm1_attr2=53496feab7f34f488185edacc70a4739"));

        //test assembly inclusion
        resp = ncmsTestBrowser.makeGET(getServerAddress() + "/ncms/asm/asmInc");
        assertEquals(200, resp.statusCode);
        respStr = resp.toString();
        assertTrue(respStr.contains("2fd82732b21646b48335e81f9b4281d5"));
        assertTrue(respStr.contains("asm1_inc=f67c7ec829b84e9da79c420f09e04994"));
        assertTrue(respStr.contains("asm1_attr1=a15e2f74f8724be9947acc586fd15a84"));
        assertTrue(respStr.contains("asm1_attr2=53496feab7f34f488185edacc70a4739"));
        assertTrue(respStr.contains("asm1_attr3=&lt;asm1_attr3&gt;"));
        assertTrue(respStr.contains("<core1>&amp;</core1>"));
        assertTrue(respStr.contains("<coreAsmInc>&amp;</coreAsmInc>"));
    }

    @Test
    public void testNotFound() throws Exception {
        //missing assembly core
        HttpTestResponse resp = ncmsTestBrowser.makeGET(getServerAddress() + "/ncms/asm/asmCoreNotFound");
        assertEquals(404, resp.statusCode);

        //missing assembly
        resp = ncmsTestBrowser.makeGET(getServerAddress() + "/ncms/asm/asmNotFound");
        assertEquals(404, resp.statusCode);
    }
}
