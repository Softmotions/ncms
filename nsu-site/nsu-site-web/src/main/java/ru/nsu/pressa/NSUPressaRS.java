package ru.nsu.pressa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
@Path("pressa")
@Produces("application/json")
@Singleton
public class NSUPressaRS {

    private final ObjectMapper mapper;

    @Inject
    public NSUPressaRS(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Path("/issues/{journal}")
    public ObjectNode issues(@PathParam("journal") String journal) {
        ObjectNode ret = mapper.createObjectNode();


        return ret;
    }
}
