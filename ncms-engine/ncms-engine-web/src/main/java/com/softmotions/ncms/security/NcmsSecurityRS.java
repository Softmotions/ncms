package com.softmotions.ncms.security;

import com.softmotions.web.security.WSGroup;
import com.softmotions.web.security.WSRole;
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
import javax.ws.rs.QueryParam;
import java.util.Iterator;

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
     *      "roles" : ["admin.asm", "admin", "user"],
     *      "groups" : [array of users groups]
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
        ArrayNode groups = res.putArray("groups");
        Iterator<WSGroup> grpiter = user.getGroups();
        while (grpiter.hasNext()) {
            groups.add(grpiter.next().getName());
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


    /**
     * Get group info list
     * <pre>
     *  [{
     *      "name":"admins",
     *      "description":"Superuser group",
     *      "roles":[array of groups rolenames]
     *  }]
     * </pre>
     *
     * @return
     */
    @GET
    @Path("groups")
    public JsonNode groups() {
        Iterator<WSGroup> groups = userDatabase.getGroups();
        ArrayNode res = mapper.createArrayNode();
        while (groups.hasNext()) {
            WSGroup g = groups.next();
            ObjectNode node = res.addObject();
            node.put("name", g.getName())
                    .put("description", g.getDescription());
            ArrayNode roles = node.putArray("roles");
            Iterator<WSRole> rolesit = g.getRoles();
            while (rolesit.hasNext()) {
                roles.add(rolesit.next().getName());
            }

        }
        return res;
    }

    /**
     * Get role info list
     * <pre>
     *  [{
     *      "name":"admins",
     *      "description":"Superuser group"
     *  }]
     * </pre>
     *
     * @return
     */
    @GET
    @Path("roles")
    public JsonNode roles() {
        Iterator<WSRole> roles = userDatabase.getRoles();
        ArrayNode res = mapper.createArrayNode();
        while (roles.hasNext()) {
            WSRole role = roles.next();
            res.addObject()
                    .put("name", role.getName())
                    .put("description", role.getDescription());
        }
        return res;
    }

    /**
     * Get users info list
     * <pre>
     *  [{
     *      "name":"admins",
     *      "description":"Superuser group"
     *  }]
     * </pre>
     *
     * @return
     */
    @GET
    @Path("users")
    public JsonNode users() {
        Iterator<WSUser> users = userDatabase.getUsers();
        ArrayNode res = mapper.createArrayNode();
        while (users.hasNext()) {
            WSUser user = users.next();
            res.addObject()
                    .put("name", user.getName())
                    .put("email", user.getEmail())
                    .put("fullName", user.getFullName());
        }
        return res;
    }

    /**
     * Get users info list from firstRow to firstRow+count
     * <pre>
     *  [{
     *      "name":"admins",
     *      "description":"Superuser group"
     *  }]
     * </pre>
     *
     * @return
     */
    @GET
    @Path("users")
    public JsonNode users(@QueryParam("firstRow") int firstRow, @QueryParam("lastRow") int lastRow) {
        Iterator<WSUser> users = userDatabase.getUsers(firstRow, Math.abs(lastRow - firstRow) + 1);
        ArrayNode res = mapper.createArrayNode();
        while (users.hasNext()) {
            WSUser user = users.next();
            res.addObject()
                    .put("name", user.getName())
                    .put("email", user.getEmail())
                    .put("fullName", user.getFullName());
        }
        return res;
    }

    /**
     * Creates group with name and description
     *
     * @return <pre>
     *  {
     *      "name":"admins",
     *      "description":"Superuser group"
     *  }
     * </pre>
     */
    @GET
    @Path("creategroup")
    public JsonNode createGroup(@QueryParam("name") String name, @QueryParam("description") String description) {
        assert (name != null) : "Parameter 'name' of group can not be empty";
        WSGroup group = userDatabase.createGroup(name, description);
        ObjectNode res = mapper.createObjectNode();
        res.put("name", group.getName());
        res.put("description", group.getDescription());
        return res;
    }

    /**
     * Creates role with name and description
     *
     * @return <pre>
     *  {
     *      "name":"admins",
     *      "description":"Description of role"
     *  }
     * </pre>
     */
    @GET
    @Path("createrole")
    public JsonNode createRole(@QueryParam("name") String name, @QueryParam("description") String description) {
        assert (name != null) : "Parameter 'name' of role can not be empty";
        WSRole role = userDatabase.createRole(name, description);
        ObjectNode res = mapper.createObjectNode();
        res.put("name", role.getName());
        res.put("description", role.getDescription());
        return res;
    }

    /**
     * Creates user with name, password and full name
     *
     * @return <pre>
     *  {
     *      "name":"admin",
     *      "password":"password",
     *      "fullName":"Иванов Иван Иванович"
     *  }
     * </pre>
     */
    @GET
    @Path("createuser")
    public JsonNode createUser(@QueryParam("name") String name, @QueryParam("password") String password, @QueryParam("fullname") String fullName) {
        assert (name != null) : "Parameter 'name' of user can not be empty";
        WSUser user = userDatabase.createUser(name, password, fullName);
        ObjectNode res = mapper.createObjectNode();
        res.put("name", user.getName());
        res.put("password", user.getPassword());
        res.put("fullName", user.getFullName());
        return res;
    }

    /**
     * Finds group of the name groupName
     * <pre>
     *  {
     *      "name":"admins",
     *      "description":"Superuser group"
     *  }
     * </pre>
     *
     * @return
     */
    @GET
    @Path("findgroup")
    public JsonNode findGroup(@QueryParam("name") String name) {
        WSGroup group = userDatabase.findGroup(name);
        ObjectNode res = null;
        if (group != null) {
            res = mapper.createObjectNode();
            res.put("name", group.getName())
                    .put("description", group.getDescription());
        }
        return res;
    }

    /**
     * Finds role of the name rolename
     * <pre>
     *  {
     *      "name":"admins",
     *      "description":"Superuser role"
     *  }
     * </pre>
     *
     * @return
     */
    @GET
    @Path("findrole")
    public JsonNode findRole(@QueryParam("name") String name) {
        WSRole role = userDatabase.findRole(name);
        ObjectNode res = null;
        if (role != null) {
            res = mapper.createObjectNode();
            res
                    .put("name", role.getName())
                    .put("description", role.getDescription());
        }
        return res;
    }

    /**
     * Finds user of the name username
     * <pre>
     *  {
     *      "name":"admin",removegroup
     *      "description":"Superuser"
     *  }
     * </pre>
     *
     * @return
     */
    @GET
    @Path("finduser")
    public JsonNode findUser(@QueryParam("name") String name) {
        WSUser user = userDatabase.findUser(name);
        ObjectNode res = null;
        if (user != null) {
            res = mapper.createObjectNode();
            res
                    .put("name", user.getName())
                    .put("fullName", user.getFullName())
                    .put("email", user.getEmail());
        }
        return res;
    }

    /**
     * Removes group of the name groupname
     */
    @GET
    @Path("removegroup")
    public void removeGroup(@QueryParam("name") String name) {
        assert (name != null) : "Parameter 'name' of group can not be empty";
        WSGroup group = userDatabase.findGroup(name);
        if (group != null) {
            userDatabase.removeGroup(group);
        }
    }

    /**
     * Removes role of the name rolename
     */
    @GET
    @Path("removerole")
    public void removeRole(@QueryParam("name") String name) {
        assert (name != null) : "Parameter 'name' of role can not be empty";
        WSRole role = userDatabase.findRole(name);
        if (role != null) {
            userDatabase.removeRole(role);
        }
    }

    /**
     * Removes user of the name username
     */
    @GET
    @Path("removeuser")
    public void removeUser(@QueryParam("name") String name) {
        assert (name != null) : "Parameter 'name' of role can not be empty";
        WSUser user = userDatabase.findUser(name);
        System.out.println("user = " + user);
        if (user != null) {
            userDatabase.removeUser(user);
        }
    }


}

