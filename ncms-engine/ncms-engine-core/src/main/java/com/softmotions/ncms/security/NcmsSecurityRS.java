package com.softmotions.ncms.security;

import java.util.Iterator;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.softmotions.ncms.jaxrs.BadRequestException;
import com.softmotions.web.security.WSGroup;
import com.softmotions.web.security.WSRole;
import com.softmotions.web.security.WSUser;
import com.softmotions.web.security.WSUserDatabase;

/**
 * Rest service for operations on users database.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Path("adm/security")
@Produces("application/json;charset=UTF-8")
public class NcmsSecurityRS {

    private static final Logger log = LoggerFactory
            .getLogger(NcmsSecurityRS.class);

    final ObjectMapper mapper;

    final WSUserDatabase userDatabase;

    @Inject
    public NcmsSecurityRS(WSUserDatabase userDatabase, ObjectMapper mapper) {
        this.userDatabase = userDatabase;
        this.mapper = mapper;
    }

    @GET
    @Path("settings")
    public JsonNode settings() {
        ObjectNode res = mapper.createObjectNode();
        res.put("usersWritable", userDatabase.isCanUsersWrite());
        res.put("usersAccessWritable", userDatabase.isCanUsersAccessWrite());
        return res;
    }

    /**
     * Return number of users stored in users database.
     */
    @GET
    @Path("users/count")
    @Produces("text/plain;charset=UTF-8")
    public Integer usersCount(@QueryParam("stext") String stext, @QueryParam("onlyActive") boolean onlyActive) {
        return onlyActive ? userDatabase.getActiveUsersCount(stext) : userDatabase.getUsersCount(stext);
    }

    /**
     * Get group info list
     * <p/>
     * <p/>
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
     * Get role info list user
     * <p/>
     * <p/>
     * <pre>
     *  [{
     *      "name":"admins",
     *      "description":"Superuser group"
     *  }]
     * </pre>
     *
     * @return(name == null)
     */
    @GET
    @Path("roles")
    public JsonNode roles() {
        Iterator<WSRole> roles = userDatabase.getRoles();
        ArrayNode res = mapper.createArrayNode();
        while (roles.hasNext()) {
            WSRole role = roles.next();
            res.addObject().put("name", role.getName())
               .put("description", role.getDescription());
        }
        return res;
    }

    /**
     * Get users info list with specified query
     * <p/>
     * <p/>
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
    public JsonNode users(@QueryParam("firstRow") int firstRow,
                          @QueryParam("lastRow") int lastRow,
                          @QueryParam("sortAsc") String ascField,
                          @QueryParam("sortDesc") String descField,
                          @QueryParam("stext") String stext,
                          @QueryParam("onlyActive") boolean onlyActive) {
        String sortField = (!StringUtils.isBlank(ascField)) ? ascField : (!StringUtils.isBlank(descField)) ? descField : null;
        int limit = firstRow == 0 && lastRow == 0 ? Integer.MAX_VALUE : Math.abs(lastRow - firstRow) + 1;
        Iterator<WSUser> users = onlyActive ?
                                 userDatabase.getActiveUsers(stext, sortField, !StringUtils.isBlank(descField), firstRow, limit) :
                                 userDatabase.getUsers(stext, sortField, !StringUtils.isBlank(descField), firstRow, limit);
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
    @POST
    @Path("group/{name}")
    public JsonNode createGroup(@PathParam("name") String name,
                                @QueryParam("description") String description) {
        if (log.isDebugEnabled()) {
            log.debug("createGroup: creategroup/{{}}?description={}", name, description);
        }
        assertion(name != null, "Parameter 'name' of group can not be empty");
        WSGroup group = userDatabase.createGroup(name, description);
        return mapper.createObjectNode()
                     .put("name", group.getName())
                     .put("description", group.getDescription());
    }

    /**
     * Creates role with name and description
     *
     * @return <pre>
     *  {
     *      "name":"admins",
     *      "description":"Description of role"
     *  }RestEasyClient
     * </pre>
     */
    @POST
    @Path("role/{name}")
    public JsonNode createRole(@PathParam("name") String name,
                               @QueryParam("description") String description) {
        if (log.isDebugEnabled()) {
            log.debug("createrole/{{}}?description={}", name, description);
        }
        assertion(name != null, "Parameter 'name' of role can not be empty");
        WSRole role = userDatabase.createRole(name, description);

        return mapper.createObjectNode()
                     .put("name", role.getName())
                     .put("description", role.getDescription());
    }

    /**
     * Updates user with name, password, full name and email
     * Creates new user if not exists
     *
     * @return <pre>
     *  {
     *      "name":"admin",
     *      "fullName":"Иванов Иван Иванович",
     *      "email":"email@email.com"
     *  }
     * </pre>
     */
    @POST
    @Path("user/{name}")
    public JsonNode updateUser(@PathParam("name") String name,
                               @QueryParam("password") String password,
                               @QueryParam("fullname") String fullName,
                               @QueryParam("email") String email) {
        if (log.isDebugEnabled()) {
            log.debug("updateuser/{{}}?password={}&fullName={}", name, password, fullName);
        }
        // if user already exists it will be returned
        WSUser user = userDatabase.createUser(name, password, fullName);

        // check for non empty password
        assertion(!StringUtils.isBlank(user.getPassword()) || !StringUtils.isBlank(password), "Can't save user without password");
        if (!StringUtils.isBlank(password)) {
            user.setPassword(StringUtils.trim(password));
        }
        // force update fields if user already exists
        user.setFullName(fullName);

        EmailValidator v = new EmailValidator();
        if (v.isValid(email, null)) { //todo iternal implementation dependent
            user.setEmail(email.toLowerCase());
        }

        return mapper.createObjectNode()
                     .put("name", user.getName())
                     .put("email", user.getEmail())
                     .put("fullName", user.getFullName());
    }

    /**
     * Finds group of the name groupName
     * <p/>
     * <p/>
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
    @Path("group/{name}")
    public JsonNode findGroup(@PathParam("name") String name) {
        if (log.isDebugEnabled()) {
            log.debug("findgroup/{{}}", name);
        }
        WSGroup group = userDatabase.findGroup(name);
        ObjectNode res = null;
        if (group != null) {
            res = mapper.createObjectNode();
            res.put("name", group.getName())
               .put("description", group.getDescription());
        }
        //noinspection ConstantConditions
        return res;
    }

    /**
     * Finds role of the name rolename
     * <p/>
     * <p/>
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
    @Path("role/{name}")
    public JsonNode findRole(@PathParam("name") String name) {
        if (log.isDebugEnabled()) {
            log.debug("findrole/{{}}", name);
        }
        WSRole role = userDatabase.findRole(name);
        ObjectNode res = null;
        if (role != null) {
            res = mapper.createObjectNode();
            res.put("name", role.getName()).put("description",
                                                role.getDescription());
        }
        //noinspection ConstantConditions
        return res;
    }

    /**
     * Get user info with specified username
     * <p/>
     * Sample JSON output:
     * <p/>
     * <p/>
     * <pre>
     *    {
     *      "name" : "admin",
     *      "email" : "adamansky@softmotions.com",
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
        ObjectNode res = mapper.createObjectNode()
                               .put("name", user.getName())
                               .put("email", user.getEmail())
                               .put("fullName", user.getFullName());

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
     * Removes group of the name groupname
     */
    @DELETE
    @Path("group/{name}")
    public void removeGroup(@PathParam("name") String name) {
        if (log.isDebugEnabled()) {
            log.debug("removegroup/{{}}", name);
        }
        assertion(name != null, "Parameter 'name' of group can not be empty");
        WSGroup group = userDatabase.findGroup(name);
        if (group != null) {
            userDatabase.removeGroup(group);
        }
    }

    /**
     * Removes role of the name rolename
     */
    @DELETE
    @Path("role/{name}")
    public void removeRole(@PathParam("name") String name) {
        if (log.isDebugEnabled()) {
            log.debug("removerole/{{}}", name);
        }
        assertion(name != null, "Parameter 'name' of role can not be empty");
        WSRole role = userDatabase.findRole(name);
        if (role != null) {
            userDatabase.removeRole(role);
        }
    }

    /**
     * Removes user of the name username
     */
    @DELETE
    @Path("user/{name}")
    public void removeUser(@PathParam("name") String name) {
        if (log.isDebugEnabled()) {
            log.debug("removeuser/{{}}", name);
        }
        assertion(name != null, "Parameter 'name' of role can not be empty");
        WSUser user = userDatabase.findUser(name);
        if (user != null) {
            userDatabase.removeUser(user);
        }
    }

    /**
     * Add role for user
     */
    @PUT
    @Path("user/{name}/role/{role}")
    public void setUserRole(@PathParam("name") String name, @PathParam("role") String roleName) {
        WSUser user = userDatabase.findUser(name);
        WSRole role = userDatabase.findRole(roleName);
        if (user != null && role != null) {
            user.addRole(role);
        }
    }

    /**
     * Unset role for user
     */
    @DELETE
    @Path("user/{name}/role/{role}")
    public void removeUserRole(@PathParam("name") String name, @PathParam("role") String roleName) {
        WSUser user = userDatabase.findUser(name);
        WSRole role = userDatabase.findRole(roleName);
        if (user != null && role != null) {
            user.removeRole(role);
        }
    }

    /**
     * Add group for user
     */
    @PUT
    @Path("user/{name}/group/{group}")
    public void setUserGroup(@PathParam("name") String name, @PathParam("group") String groupName) {
        WSUser user = userDatabase.findUser(name);
        WSGroup group = userDatabase.findGroup(groupName);
        if (user != null && group != null) {
            user.addGroup(group);
        }
    }

    /**
     * Unset group for user
     */
    @DELETE
    @Path("user/{name}/group/{group}")
    public void removeUserGroup(@PathParam("name") String name, @PathParam("group") String groupName) {
        WSUser user = userDatabase.findUser(name);
        WSGroup group = userDatabase.findGroup(groupName);
        if (user != null && group != null) {
            user.removeGroup(group);
        }
    }

    private void assertion(boolean noError, String message) {
        if (!noError) {
            throw new BadRequestException(message);
        }
    }

}
