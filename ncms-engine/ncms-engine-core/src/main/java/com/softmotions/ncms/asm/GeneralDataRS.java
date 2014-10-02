package com.softmotions.ncms.asm;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.commons.cont.Pair;
import com.softmotions.web.ResponseUtils;
import com.softmotions.weboot.mb.MBDAOSupport;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
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
import java.sql.Blob;
import java.util.Map;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
@Path("ad")
@Singleton
public class GeneralDataRS extends MBDAOSupport implements GeneralDataService {

    public static final String PAGE_PDF_REF_TEMPLATE = "page.pdf:{id}";

    private final AsmDAO adao;

    @Inject
    public GeneralDataRS(SqlSession sess, AsmDAO adao) {
        super(GeneralDataRS.class.getName(), sess);
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

        Pair<String, InputStream> pdfData = getAdditionalData(PAGE_PDF_REF_TEMPLATE.replace("{id}", String.valueOf(id)));
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
        saveAdditionalData(PAGE_PDF_REF_TEMPLATE.replace("{id}", String.valueOf(id)), data, "application/pdf");
    }

    public boolean isPagePdfExists(Long id) {
        return isAdditionalDataExists(PAGE_PDF_REF_TEMPLATE.replace("{id}", String.valueOf(id)));
    }

    public void removePagePdf(Long id) {
        removeAdditionalData(PAGE_PDF_REF_TEMPLATE.replace("{id}", String.valueOf(id)));
    }

    @Transactional
    public Pair<String, InputStream> getAdditionalData(String ref) throws Exception {
        Map<String, Object> row = selectOne("getData", ref);
        if (row == null) {
            return null;
        }

        String type = (String) row.get("content_type");
        Blob data = (Blob) row.get("data");

        return type == null || data == null ? null : new Pair(type, data.getBinaryStream());
    }

    @Transactional
    public boolean isAdditionalDataExists(String ref) {
        if (StringUtils.isBlank(ref)) {
            throw new IllegalArgumentException("ref");
        }
        return ((Long) selectOne("checkDataExists", StringUtils.trim(ref))) > 0;
    }

    @Transactional
    public void saveAdditionalData(String ref, byte[] data, String type) {
        if (StringUtils.isBlank(ref)) {
            throw new IllegalArgumentException("ref");
        }
        if (StringUtils.isBlank(type)) {
            throw new IllegalArgumentException("type");
        }

        if (data == null) {
            data = ArrayUtils.EMPTY_BYTE_ARRAY;
        }

        update("saveData", "ref", ref, "data", data, "content_type", type);
    }

    @Transactional
    public void removeAdditionalData(String ref) {
        if (StringUtils.isBlank(ref)) {
            throw new IllegalArgumentException("ref");
        }
        update("removeData", StringUtils.trim(ref));
    }
}
