package ru.nsu.pagepdf;

import com.softmotions.commons.cont.Pair;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.ds.GeneralDataStore;
import com.softmotions.web.ResponseUtils;

import com.google.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.mybatis.guice.transactional.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
@Path("ad")
public class PagePdfRS {

    public static final String PAGE_PDF_REF_TEMPLATE = "page.pdf:{id}";

    private final GeneralDataStore ds;

    private final AsmDAO adao;

    @Inject
    public PagePdfRS(GeneralDataStore ds, AsmDAO adao) {
        this.ds = ds;
        this.adao = adao;
    }

    @GET
    @Path("/pagepdf/{id}")
    @Transactional
    public Response getPagePdf(@Context HttpServletRequest req,
                               @PathParam("id") Long id) throws Exception {

        Asm asm = adao.asmSelectById(id);
        if (asm == null) {
            throw new NotFoundException();
        }

        Response.ResponseBuilder rb = Response.ok();

        Pair<String, InputStream> pdfData = ds.getData(PAGE_PDF_REF_TEMPLATE.replace("{id}", String.valueOf(id)));
        if (pdfData == null) {
            rb.status(Response.Status.NO_CONTENT);
        } else {
            rb.type(pdfData.getOne());
            rb.header(HttpHeaders.CONTENT_DISPOSITION, ResponseUtils.encodeContentDisposition(asm.getHname(), null != req.getParameter("inline")));

            rb.entity((StreamingOutput) output -> {
                try (final InputStream fis = pdfData.getTwo()) {
                    IOUtils.copyLarge(fis, output);
                }
            });
        }

        return rb.build();
    }

    public void savePagePdf(Long id, byte[] data) {
        ds.saveData(PAGE_PDF_REF_TEMPLATE.replace("{id}", String.valueOf(id)), data, "application/pdf");
    }

    public boolean isPagePdfExists(Long id) {
        return ds.isDataExists(PAGE_PDF_REF_TEMPLATE.replace("{id}", String.valueOf(id)));
    }

    public void removePagePdf(Long id) {
        ds.removeData(PAGE_PDF_REF_TEMPLATE.replace("{id}", String.valueOf(id)));
    }

}
