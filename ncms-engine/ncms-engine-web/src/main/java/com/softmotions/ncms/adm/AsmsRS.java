package com.softmotions.ncms.adm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Assemblies selector.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Path("adm/asms")
public class AsmsRS {

    @Inject
    ObjectMapper mapper;

    @GET
    @Path("select")
    @Produces("application/json")
    public JsonNode select() {
        ArrayNode asmList = mapper.createArrayNode();
        //todo
        return asmList;
    }

    @GET
    @Path("select/count")
    @Produces("text/plain")
    public Integer count() {
        //todo
        return 0;
    }

}
