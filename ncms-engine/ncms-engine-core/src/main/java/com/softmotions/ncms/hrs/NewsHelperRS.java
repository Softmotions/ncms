package com.softmotions.ncms.hrs;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.inject.Inject;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.CachedPage;
import com.softmotions.ncms.asm.PageCriteria;
import com.softmotions.ncms.asm.PageService;

/**
 * News helper REST API.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Path("/helpers/news")
@Produces("application/json;charset=UTF-8")
public class NewsHelperRS {

    private final PageService pageService;

    private final AsmDAO adao;

    @Inject
    public NewsHelperRS(PageService pageService,
                        AsmDAO adao) {
        this.pageService = pageService;
        this.adao = adao;
    }

    /**
     * Retrieve list of published news for page as JSON array.
     * <p>
     * Every element in news list should contains:
     * <p>
     * <pre>
     * {
     *  guid:  {String} News item guid,
     *  hname: {String} News page human name ({@link CachedPage#getHname()})
     *  brief: {String} News page brief HTML (cp.getAsm().getEffectiveAttributeAsString("brief", ""))
     * </pre>
     * }
     *
     * @param guidOrAlias News root page guid or alias
     * @param offset      News list offset
     * @param limit       News list limit
     * @return
     */
    @GET
    @Path("/page/{guidOrAlias}/list")
    public Response newsForPage(@PathParam("guidOrAlias") String guidOrAlias,
                                @QueryParam("offset") @DefaultValue("0") Long offset,
                                @QueryParam("limit") @DefaultValue("1000") Long limit) throws Exception {

        CachedPage cp = pageService.getCachedPage(guidOrAlias, true);
        if (cp == null) {
            throw new NotFoundException();
        }

        PageCriteria crit = adao.newPageCriteria();
        crit.withNavParentId(cp.getId());
        crit.withPublished(true);
        crit.skip(offset.intValue());
        crit.limit(limit.intValue());
        crit.onAsm().orderBy("ordinal").desc();

        return Response.ok((StreamingOutput) output -> {
            final JsonGenerator gen = new JsonFactory().createGenerator(output);
            gen.writeStartArray();

            for (Asm child : crit.selectAsAsms()) {
                gen.writeStartObject();
                gen.writeStringField("guid", child.getName());
                gen.writeStringField("hname", child.getHname());
                gen.writeStringField("brief", child.getEffectiveAttributeAsString("brief", ""));
                gen.writeEndObject();
            }

            gen.writeEndArray();
            gen.flush();
        }).type("application/json;charset=UTF-8")
                       .build();
    }


}
