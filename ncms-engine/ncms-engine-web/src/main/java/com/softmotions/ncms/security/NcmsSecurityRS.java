package com.softmotions.ncms.security;

import java.util.Iterator;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

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
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Path("adm/security")
@Produces("application/json")
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

	/**
	 * Get user info with specified username
	 * <p/>
	 * Sample JSON output:
	 * <p/>
	 * 
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
	 * @param name
	 *            Name of user
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
	 * Return number of users stored in users database.
	 */
	@GET
	@Path("users/count")
	@Produces("text/plain")
	public Integer usersCount() {
		return userDatabase.getUsersCount();
	}

	/**
	 * Get group info list
	 * <p/>
	 * 
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
	 * 
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
	 * Get users info list
	 * <p/>
	 * 
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
			res.addObject().put("name", user.getName())
			        .put("email", user.getEmail())
			        .put("fullName", user.getFullName());
		}
		return res;
	}

	/**
	 * Get users info list from firstRow to firstRow+count
	 * <p/>
	 * 
	 * <pre>
	 *  [{
	 *      "name":"admins",
	 *      "description":"Superuser group"RestEasyClient
	 *  }]
	 * </pre>
	 * 
	 * @return
	 */
	@POST
	@Path("users")
	public JsonNode users(@QueryParam("firstRow") int firstRow,
	        @QueryParam("lastRow") int lastRow) {
		if (log.isDebugEnabled()) {
			log.debug("users?firstRow=" + firstRow + "&lastRow=" + lastRow);
		}
		Iterator<WSUser> users = userDatabase.getUsers(firstRow,
		        Math.abs(lastRow - firstRow) + 1);
		ArrayNode res = mapper.createArrayNode();
		while (users.hasNext()) {
			WSUser user = users.next();
			res.addObject().put("name", user.getName())
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
			log.debug("createGroup: creategroup/{" + name + "}?description="
			        + description);
		}
		assertion(name != null, "Parameter 'name' of group can not be empty");
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
	 *  }RestEasyClient
	 * </pre>
	 */
	@POST
	@Path("role/{name}")
	public JsonNode createRole(@PathParam("name") String name,
	        @QueryParam("description") String description) {
		if (log.isDebugEnabled()) {
			log.debug("createrole/{" + name + "}?description=" + description);
		}
		assertion(name != null, "Parameter 'name' of role can not be empty");
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
	@POST
	@Path("user/{name}")
	public JsonNode createUser(@PathParam("name") String name,
	        @QueryParam("password") String password,
	        @QueryParam("fullname") String fullName) {
		if (log.isDebugEnabled()) {
			log.debug("createuser/{" + name + "}?password=" + password
			        + "&fullName=" + fullName);
		}
		assertion(name != null && password != null,
		        "Parameters 'name' and 'password' of user can not be empty");
		WSUser user = userDatabase.createUser(name, password, fullName);
		ObjectNode res = mapper.createObjectNode();
		res.put("name", user.getName());
		res.put("password", user.getPassword());
		res.put("fullName", user.getFullName());
		return res;
	}

	/**
	 * Finds group of the name groupName
	 * <p/>
	 * 
	 * <pre>
	 *  {
	 *      "name":"admins",PUT
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
			log.debug("findgroup/{" + name + "}");
		}
		WSGroup group = userDatabase.findGroup(name);
		ObjectNode res = null;
		if (group != null) {
			res = mapper.createObjectNode();
			res.put("name", group.getName()).put("description",
			        group.getDescription());
		}
		return res;
	}

	/**
	 * Finds role of the name rolename
	 * <p/>
	 * 
	 * <pre>
	 *  {
	 *      "name":"admins",com.softmotions.ncms.security.NcmsSecurityRSTest
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
			log.debug("findrole/{" + name + "}");
		}
		WSRole role = userDatabase.findRole(name);
		ObjectNode res = null;
		if (role != null) {
			res = mapper.createObjectNode();
			res.put("name", role.getName()).put("description",
			        role.getDescription());
		}
		return res;
	}

	/**
	 * Finds user of the name username
	 * <p/>
	 * 
	 * <pre>
	 * {
	 * "name":"admin",removegroup
	 * "description":"Superuser"
	 * }
	 * </pre>
	 * 
	 * @return
	 */
	@GET
	@Path("user/{name}")
	public JsonNode findUser(@PathParam("name") String name) {
		if (log.isDebugEnabled()) {
			log.debug("finduser/{" + name + "}");
		}
		WSUser user = userDatabase.findUser(name);
		ObjectNode res = null;
		if (user != null) {
			res = mapper.createObjectNode();
			res.put("name", user.getName()).put("fullName", user.getFullName())
			        .put("email", user.getEmail());
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
			log.debug("removegroup/{" + name + "}");
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
			log.debug("removerole/{" + name + "}");
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
			log.debug("removeuser/{" + name + "}");
		}
		assertion(name != null, "Parameter 'name' of role can not be empty");
		WSUser user = userDatabase.findUser(name);
		if (user != null) {
			userDatabase.removeUser(user);
		}
	}

	private void assertion(boolean noError, String message) {
		if (!noError) {
			throw new BadRequestException(message);
		}
	}

}
