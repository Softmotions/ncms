package com.softmotions.ncms.security;

import com.softmotions.web.security.WSUser;
import com.softmotions.web.security.WSUserDatabase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * Rest service for operations on
 * users database.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Path("adm/security")
@Produces("application/json")

public class NcmsSecurityRS {

    final ObjectMapper mapper;

    final WSUserDatabase userDatabase;

    @Inject
    public NcmsSecurityRS(WSUserDatabase userDatabase, ObjectMapper mapper) {
        this.userDatabase = userDatabase;
        this.mapper = mapper;
    }

    /**
     * Get user info with specified username
     * <p/>
     * Sample JSON output:
     * <pre>
     *    {
     *      "name" : "admin",
     *      "email" : "adamansky@gmail.com",
     *      "fullName" : "Антон Адаманский",
     *      "roles" : ["admin.asm", "admin", "user"]
     *    }
     * </pre>
     *
     * @param name Name of user
     * @return
     */
    @GET
    @Path("user/{name}")
    public JsonNode userGet(@PathParam("name") String name) {
        WSUser user = userDatabase.findUser(name);
        if (user == null) {
            throw new NotFoundException(name);
        }
        ObjectNode res = mapper.createObjectNode();
        res.put("name", user.getName());
        res.put("email", user.getEmail());
        res.put("fullName", user.getFullName());
        ArrayNode roles = res.putArray("roles");
        for (final String r : user.getRoleNames()) {
            roles.add(r);
        }
        return res;
    }

    /**
     * Return number of users
     * stored in users database.
     */
    @GET
    @Path("users/count")
    @Produces("text/plain")
    public Integer usersCount() {
        return userDatabase.getUsersCount();
    }

    //todo
    //
    //
    //
    //
}

