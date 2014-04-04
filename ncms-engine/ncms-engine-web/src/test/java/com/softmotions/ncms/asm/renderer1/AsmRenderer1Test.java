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

/**
 * Test basics of assembly rendering.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmRenderer1Test extends NcmsWebTest {

    private static final Logger log = LoggerFactory.getLogger(AsmRenderer1Test.class);

    protected void afterServerStart() throws Exception {
        AsmDAO adao = getInjector().getInstance(AsmDAO.class);

        Asm asm = new Asm("asm1",
                          new AsmCore("com/softmotions/ncms/asm/renderer1/core1.txt", "Core1"));
        asm.addAttribute(new AsmAttribute("asm1_attr1", "a15e2f74f8724be9947acc586fd15a84"));
        asm.addAttribute(new AsmAttribute("asm1_attr2", "53496feab7f34f488185edacc70a4739"));
        adao.asmInsert(asm);
    }

    @Test
    public void testRendererBasic() throws Exception {
        HttpTestResponse resp = ncmsTestBrowser.makeGET(getServerAddress() + "/ncms/asm/asm1");
        log.info("SCODE=" + resp.statusCode);
        log.info("CHARSET=" + resp.charset);
        log.info("RESP=" + resp);

    }
}
