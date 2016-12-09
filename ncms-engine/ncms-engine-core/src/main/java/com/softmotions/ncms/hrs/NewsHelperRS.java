package com.softmotions.ncms.hrs;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.softmotions.ncms.asm.CachedPage;
import com.softmotions.ncms.asm.PageService;

/**
 * News helper REST API.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Path("/helpers/news")
@Produces("application/json;charset=UTF-8")
public class NewsHelperRS {

    private final ObjectMapper mapper;

    private final PageService pageService;

    @Inject
    public NewsHelperRS(PageService pageService,
                        ObjectMapper mapper) {
        this.pageService = pageService;
        this.mapper = mapper;
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
    @Path("/page/{guidOrAlias}/list")
    public Response newsForPage(@PathParam("guidOrAlias") String guidOrAlias,
                                @QueryParam("offset") @DefaultValue("0") Long offset,
                                @QueryParam("limit") @DefaultValue("limit") Long limit) throws Exception {

        CachedPage cp = pageService.getCachedPage(guidOrAlias, true);
        if (cp == null) {
            throw new NotFoundException();
        }

        return Response.ok((StreamingOutput) output -> {
            final JsonGenerator gen = new JsonFactory().createGenerator(output);
            gen.writeStartArray();

            // todo

            gen.writeEndArray();
            gen.flush();
        }).type("application/json;charset=UTF-8")
                       .build();
    }


}
