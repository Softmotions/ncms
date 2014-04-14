package com.softmotions.ncms.asm.renderer1;

import com.google.inject.Inject;
import com.softmotions.ncms.HttpTestResponse;
import com.softmotions.ncms.NcmsWebTest;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmCore;
import com.softmotions.ncms.asm.AsmDAO;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test basics of assembly rendering.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmRenderer1Test extends NcmsWebTest {

    private static final Logger log = LoggerFactory.getLogger(AsmRenderer1Test.class);

    @Inject
    AsmDAO adao;

    public void initContext(ServletContextHandler context) {
        context.addServlet(TestResponseServlet.class, "/testresp");
    }

    protected void afterServerStart() throws Exception {
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
        Asm asmInc = new Asm("asmInc",
                             new AsmCore("com/softmotions/ncms/asm/renderer1/coreAsmInc.httl", "AsmIncCore"));
        asmInc.addAttribute(new AsmAttribute("asm1_inc", "asmref", "asm1"));
        adao.asmInsert(asmInc);


        //Another version of assembly inclusion
        Asm asmInc2 = new Asm("asmInc2",
                              new AsmCore("com/softmotions/ncms/asm/renderer1/coreAsmInc2.httl", "coreAsmInc2"));
        asmInc2.addAttribute(new AsmAttribute("internal_inc1", "resource",
                                              "/testresp"));
        asmInc2.addAttribute(new AsmAttribute("internal_inc2", "resource",
                                              "/testresp?dc6bda8275b2=4c3e&6b21e2ee=a9cb"));
        asmInc2.addAttribute(new AsmAttribute("external_inc1", "resource",
                                              getServerAddress() + "/testresp?d499e094=3bc8"));


        adao.asmInsert(asmInc2);
        adao.asmSetParent(asmInc2, asmInc);
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

        resp = ncmsTestBrowser.makeGET(getServerAddress() + "/testresp");
        assertEquals(200, resp.statusCode);
        assertEquals("UTF-8", resp.charset);
        respStr = resp.toString();
        assertTrue(respStr.contains("0f7542de52b847b68ea6a16a7762c560"));

        resp = ncmsTestBrowser.makeGET(getServerAddress() + "/ncms/asm/asmInc2");
        respStr = resp.toString();
        assertEquals(200, resp.statusCode);
        assertEquals("UTF-8", resp.charset);
        assertTrue(respStr.contains("b7d52d51edf94942a09fdda98ed88020"));
        assertTrue(respStr.contains("internal_inc1=0f7542de52b847b68ea6a16a7762c560"));
        assertTrue(respStr.contains("4b5c=83f4"));
        assertTrue(respStr.contains("internal_inc2=0f7542de52b847b68ea6a16a7762c560"));
        assertTrue(respStr.contains("dc6bda8275b2=4c3e"));
        assertTrue(respStr.contains("6b21e2ee=a9cb"));
        assertTrue(respStr.contains("internal_inc11=0f7542de52b847b68ea6a16a7762c560"));
        assertTrue(respStr.contains("e71f=4737"));
        assertTrue(respStr.contains("internal_inc111=0f7542de52b847b68ea6a16a7762c560"));
        assertTrue(respStr.contains("42b7=8729"));
        assertTrue(respStr.contains("440d=b6dc"));
        assertTrue(respStr.contains("asm1_inc=f67c7ec829b84e9da79c420f09e04994"));
        assertTrue(respStr.contains("asm1_attr3=&lt;asm1_attr3&gt;"));
        assertTrue(respStr.contains("<core1>&amp;</core1>"));
        assertTrue(respStr.contains("external_inc1=0f7542de52b847b68ea6a16a7762c560"));
        assertTrue(respStr.contains("d499e094=3bc8"));
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


    public static class TestResponseServlet extends HttpServlet {

        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/html; charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("0f7542de52b847b68ea6a16a7762c560");
            Enumeration<String> pnames = req.getParameterNames();
            while (pnames.hasMoreElements()) {
                String pname = pnames.nextElement();
                out.println(pname + '=' + req.getParameter(pname));
            }
        }
    }
}
