package com.softmotions.ncms.security;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Test;

import com.softmotions.ncms.NcmsWebTest;

/**
 * @author Savelev Dmitry (dd.savelev@gmail.com)
 */
public class NcmsSecurityRSTest extends NcmsWebTest {

	public NcmsSecurityRSTest() {
	}

	@Test
	public void testAll() throws Exception {
		String address = getServerAddress();

		/* ------------ user --------------- */
		/* ------------ test create user ------------------- */
		WebTarget wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/user/test-user");
		wt.queryParam("password", "password")
		        .queryParam("fullName", "fullName");
		Response resp = wt.request()
		        .buildPost(Entity.entity(String.class, MediaType.TEXT_PLAIN))
		        .invoke();
		String data = resp.readEntity(String.class);
		log.info("!!!!!!!!!!!!!!!!!!!! user/test-user/create=" + data);
		assertEquals(200, resp.getStatus());

		/* ------------ test get user ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/user/test-user");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		log.info("!!!!!!!!!!!!!!!!!!!! user/test-user/get=" + data);
		assertEquals(200, resp.getStatus());

		/* ------------ test find user ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/user/test-user");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		log.info("!!!!!!!!!!!!!!!!!!!! user/test-user/find=" + data);
		assertEquals(200, resp.getStatus());

		/* ------------ test user count ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/users/count");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		log.info("!!!!!!!!!!!!!!!!!!!! users/count=" + data);
		assertEquals(200, resp.getStatus());

		/* ------------ test users ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/users");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		log.info("!!!!!!!!!!!!!!!!!!!! users/get=" + data);
		assertEquals(200, resp.getStatus());

		/* ------------ test users ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/users");
		wt.queryParam("firstRow", "0").queryParam("lastRow", "20");
		resp = wt.request()
		        .buildPost(Entity.entity(String.class, MediaType.TEXT_PLAIN))
		        .invoke();
		data = resp.readEntity(String.class);
		log.info("!!!!!!!!!!!!!!!!!!!! users/get/pagination=" + data);
		assertEquals(200, resp.getStatus());

		/* ------------ test delete user ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/user/test-user");
		resp = wt.request().buildDelete().invoke();
		log.info("!!!!!!!!!!!!!!!!!!!! user/test-user/delete");
		assertEquals(204, resp.getStatus());

		/* ------------ role --------------- */
		/* ------------ test create role ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/role/test-role");
		wt.queryParam("description", "test-role-description");
		resp = wt.request()
		        .buildPost(Entity.entity(String.class, MediaType.TEXT_PLAIN))
		        .invoke();
		data = resp.readEntity(String.class);
		log.info("!!!!!!!!!!!!!!!!!!!! role/test-role/create=" + data);
		assertEquals(200, resp.getStatus());

		/* ------------ test get role ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/role/test-role");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		log.info("!!!!!!!!!!!!!!!!!!!! role/test-role/get=" + data);
		assertEquals(200, resp.getStatus());

		/* ------------ test find role ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/role/test-role");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		log.info("!!!!!!!!!!!!!!!!!!!! role/test-role/find=" + data);
		assertEquals(200, resp.getStatus());

		/* ------------ test roles ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/roles");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		log.info("!!!!!!!!!!!!!!!!!!!! roles/get=" + data);
		assertEquals(200, resp.getStatus());

		/* ------------ test delete role ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/role/test-role");
		resp = wt.request().buildDelete().invoke();
		log.info("!!!!!!!!!!!!!!!!!!!! role/test-role/delete");
		assertEquals(204, resp.getStatus());

		/* ------------ test roles ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/roles");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		log.info("!!!!!!!!!!!!!!!!!!!! roles/get=" + data);
		assertEquals(200, resp.getStatus());

		/* ------------ group --------------- */
		/* ------------ test create group ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/group/test-group");
		wt.queryParam("description", "test-group-description");
		resp = wt.request()
		        .buildPost(Entity.entity(String.class, MediaType.TEXT_PLAIN))
		        .invoke();
		data = resp.readEntity(String.class);
		log.info("!!!!!!!!!!!!!!!!!!!! group/test-group/create=" + data);
		assertEquals(200, resp.getStatus());

		/* ------------ test find group ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/group/test-group");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		log.info("!!!!!!!!!!!!!!!!!!!! group/test-group/find=" + data);
		assertEquals(200, resp.getStatus());

		/* ------------ test groups ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/groups");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		log.info("!!!!!!!!!!!!!!!!!!!! groups/get=" + data);
		assertEquals(200, resp.getStatus());

		/* ------------ test delete group ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/group/test-group");
		resp = wt.request().buildDelete().invoke();
		log.info("!!!!!!!!!!!!!!!!!!!! group/test-group/delete");
		assertEquals(204, resp.getStatus());

		/* ------------ test groups ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + "/ncms/rs/adm/security/groups");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		log.info("!!!!!!!!!!!!!!!!!!!! groups/get=" + data);
		assertEquals(200, resp.getStatus());
	}

	/*
	 * public void initServer(Server server, ServletContextHandler context) {
	 * super.initServer(server, context); }
	 * 
	 * private String getServerUrl(String basePath, String entityType, String
	 * action) { return getServerAddress() + TEST_URI + basePath + entityType +
	 * ((action != null) ? action : ""); }
	 * 
	 * private String getServerUrl(String basePath, String entityType) { return
	 * getServerUrl(basePath, entityType, null); }
	 * 
	 * private WebTarget getWebTarget(String url, Map<String, String> params) {
	 * Client client = ClientBuilder.newClient(); WebTarget request =
	 * client.target(url); if (params != null) { for (String param :
	 * params.keySet()) { request.queryParam(param, params.get(param)); } }
	 * return request; }
	 * 
	 * private String callGetMethod(String url, Map<String, String> params) {
	 * try { WebTarget request = getWebTarget(url, params); return
	 * request.request(MediaType.TEXT_PLAIN).get().toString(); } catch
	 * (Throwable e) { e.printStackTrace(); throw e; } }
	 * 
	 * private String callGetMethod(String url) { return callGetMethod(url,
	 * null); }
	 * 
	 * private String callPostMethod(String url, Map<String, String> params) {
	 * try { WebTarget request = getWebTarget(url, params); return
	 * request.request(MediaType.TEXT_PLAIN).post(null, String.class); } catch
	 * (Throwable e) { e.printStackTrace(); throw e; } }
	 * 
	 * private String callPostMethod(String url) { return callPostMethod(url,
	 * null); }
	 * 
	 * private void callDeleteMethod(String url, Map<String, String> params) {
	 * try { WebTarget request = getWebTarget(url, params);
	 * request.request(MediaType.TEXT_PLAIN).delete(); } catch (Throwable e) {
	 * e.printStackTrace(); throw e; } }
	 * 
	 * private void callDeleteMethod(String url) { callDeleteMethod(url, null);
	 * }
	 * 
	 * private boolean checkEntity(String entityType, String entity) { return
	 * callGetMethod( getServerUrl("/security", "/find" + entityType, "/" +
	 * entity)) .contains(entity); }
	 * 
	 * private void removeEntity(String entityType, String entity) {
	 * callDeleteMethod(getServerUrl("/security", "/remove" + entityType, "/" +
	 * entity)); }
	 * 
	 * private void putEntity(String entityType, String entity, Map<String,
	 * String> params) { String result = callPostMethod(
	 * getServerUrl("/security", "/create" + entityType, "/" + entity), params);
	 * assertTrue("Such " + entityType + " of " + entity + " was not added",
	 * result.contains(entity)); }
	 * 
	 * private void createUser(String entity, String passord, String fullName)
	 * throws Exception { Map<String, String> params = new HashMap<String,
	 * String>(); params.put("password", passord); params.put("fullname",
	 * fullName); putEntity("user", entity, params); }
	 * 
	 * private void removeUser(String entity) throws Exception {
	 * removeEntity("user", entity); }
	 * 
	 * // //@Test public void testRemoveUser() throws Exception {
	 * createUser("test-user", "password", "Dmitry Savelev");
	 * assertTrue("User test-user was not created", !checkEntity("user",
	 * "test-user")); removeUser("test-user"); //
	 * assertTrue("User test-user was not removed", !checkEntity("user", //
	 * "test-user")); }
	 * 
	 * @Test public void testCreateUser() throws Exception {
	 * removeUser("test-user"); // assertTrue("User test-user was not removed",
	 * // !checkEntity("user", "test-user")); createUser("test-user",
	 * "password", "Dmitry Savelev"); //
	 * assertTrue("User test-user was not created", // !checkEntity("user",
	 * "test-user")); // removeUser("test-user"); //
	 * assertTrue("User test-user was not removed", // !checkEntity("user",
	 * "test-user")); }
	 * 
	 * // @Test public void testFindUser() throws Exception {
	 * removeUser("test-user"); // assertTrue("User test-user was not removed",
	 * !checkEntity("user", // "test-user")); createUser("test-user",
	 * "password", "Dmitry Savelev"); //
	 * assertTrue("User test-user was not created", !checkEntity("user", //
	 * "test-user")); removeUser("test-user"); //
	 * assertTrue("User test-user was not removed", !checkEntity("user", //
	 * "test-user")); }
	 * 
	 * // @Test public void testGetUser() throws Exception {
	 * removeUser("test-user"); // assertTrue("User test-user was not removed",
	 * !checkEntity("user", // "test-user")); createUser("test-user",
	 * "password", "Dmitry Savelev"); //
	 * assertTrue("User test-user was not created", !checkEntity("user", //
	 * "test-user")); // checkEntity("user", "test-user");
	 * removeUser("test-user"); // assertTrue("User test-user was not removed",
	 * !checkEntity("user", // "test-user")); }
	 * 
	 * // @Test public void testUsers() throws Exception {
	 * removeUser("test-user"); // assertTrue("User test-user was not removed",
	 * !checkEntity("user", // "test-user")); createUser("test-user",
	 * "password", "Dmitry Savelev"); //
	 * assertTrue("User test-user was not created", !checkEntity("user", //
	 * "test-user"));
	 * 
	 * String result = callGetMethod(getServerUrl("/security", "/users"));
	 * assertTrue(result.contains("test-user"));
	 * 
	 * removeUser("test-user"); // assertTrue("User test-user was not removed",
	 * !checkEntity("user", // "test-user")); }
	 * 
	 * // @Test public void testUsersPagination() throws Exception {
	 * removeUser("test-user"); assertTrue("User test-user was not removed",
	 * !checkEntity("user", "test-user")); createUser("test-user", "password",
	 * "Dmitry Savelev"); // assertTrue("User test-user was not created",
	 * !checkEntity("user", // "test-user"));
	 * 
	 * Map<String, String> params = new HashMap<String, String>();
	 * params.put("firstRow", "1"); params.put("lastRow", "1"); String result =
	 * callGetMethod(getServerUrl("/security", "/users"), params);
	 * assertTrue(result.contains("test-user"));
	 * 
	 * removeUser("test-user"); // assertTrue("User test-user was not removed",
	 * !checkEntity("user", // "test-user")); }
	 * 
	 * // @Test public void testUserCount() throws Exception {
	 * removeUser("test-user"); // assertTrue("User test-user was not removed",
	 * !checkEntity("user", // "test-user"));
	 * 
	 * String result = callGetMethod( getServerUrl("/security", "/users",
	 * "/count"), null); assertTrue(NumberUtils.isNumber(result)); long count =
	 * NumberUtils.createLong(result);
	 * 
	 * createUser("test-user", "password", "Dmitry Savelev"); //
	 * assertTrue("User test-user was not removed", !checkEntity("user", //
	 * "test-user"));
	 * 
	 * result = callGetMethod(getServerUrl("/security", "/users", "/count"),
	 * null); assertTrue(NumberUtils.isNumber(result));
	 * assertTrue(NumberUtils.createLong(result) == count + 1);
	 * 
	 * removeUser("test-user"); // assertTrue("User test-user was not removed",
	 * !checkEntity("user", // "test-user")); }
	 * 
	 * private void createGroup(String entity, String description) throws
	 * Exception { Map<String, String> params = new HashMap<String, String>();
	 * params.put("description", description); putEntity("group", entity,
	 * params); }
	 * 
	 * private void removeGroup(String entity) { removeEntity("group", entity);
	 * }
	 * 
	 * // @Test public void testRemoveGroup() throws Exception {
	 * removeGroup("test-group"); //
	 * assertTrue("Group test-group was not removed", !checkEntity("group", //
	 * "test-group")); }
	 * 
	 * // @Test public void testCreateGroup() throws Exception {
	 * removeGroup("test-group"); //
	 * assertTrue("Group test-group was not removed", !checkEntity("group", //
	 * "test-group")); createGroup("test-group", "Group for test"); //
	 * assertTrue("Group test-group was not created", checkEntity("group", //
	 * "test-group")); removeGroup("test-group"); //
	 * assertTrue("Group test-group was not removed", !checkEntity("group", //
	 * "test-group")); }
	 * 
	 * // @Test public void testFindGroup() throws Exception {
	 * removeGroup("test-group"); assertTrue("Group test-group was not removed",
	 * !checkEntity("group", "test-group")); createGroup("test-group",
	 * "Group for test"); assertTrue("Group test-group was not created",
	 * checkEntity("group", "test-group")); removeGroup("test-group");
	 * assertTrue("Group test-group was not removed", checkEntity("group",
	 * "test-group")); }
	 * 
	 * // @Test public void testGroups() throws Exception {
	 * removeGroup("test-group"); //
	 * assertTrue("Group test-group was not removed", !checkEntity("group", //
	 * "test-group")); createGroup("test-group", "Group for test"); //
	 * assertTrue("Group test-group was not created", checkEntity("group", //
	 * "test-group"));
	 * 
	 * String result = callGetMethod(getServerUrl("/security", "/groups"));
	 * assertTrue(result.contains("test-group"));
	 * 
	 * removeGroup("test-group"); //
	 * assertTrue("Group test-group was not removed", checkEntity("group", //
	 * "test-group")); }
	 * 
	 * private void createRole(String entity, String description) throws
	 * Exception { Map<String, String> params = new HashMap<String, String>();
	 * params.put("description", description); putEntity("role", entity,
	 * params); }
	 * 
	 * private void removeRole(String entity) { removeEntity("role", entity); }
	 * 
	 * // @Test public void testRemoveRole() throws Exception {
	 * removeRole("test-role"); // assertTrue("Role test-role was not removed",
	 * !checkEntity("role", // "test-role")); }
	 * 
	 * // @Test public void testCreateRole() throws Exception {
	 * removeRole("test-role"); // assertTrue("Role test-role was not removed",
	 * !checkEntity("role", // "test-role")); createRole("test-role",
	 * "Role for test"); // assertTrue("Role test-role was not created",
	 * checkEntity("role", // "test-role")); removeRole("test-role"); //
	 * assertTrue("Role test-role was not removed", !checkEntity("role", //
	 * "test-role")); }
	 * 
	 * // @Test public void testFindRole() throws Exception {
	 * removeRole("test-role"); // assertTrue("Role test-role was not removed",
	 * !checkEntity("role", // "test-role")); createRole("test-role",
	 * "Role for test"); // assertTrue("Role test-role was not created",
	 * checkEntity("role", // "test-role")); removeRole("test-role"); //
	 * assertTrue("Role test-role was not removed", checkEntity("role", //
	 * "test-role")); }
	 * 
	 * // @Test public void testRoles() throws Exception {
	 * removeRole("test-role"); // assertTrue("Role test-role was not removed",
	 * !checkEntity("role", // "test-role")); createRole("test-role",
	 * "Role for test"); // assertTrue("Role test-role was not created",
	 * checkEntity("role", // "test-role"));
	 * 
	 * String result = callGetMethod(getServerUrl("/security", "/roles"));
	 * assertTrue(result.contains("test-role"));
	 * 
	 * removeRole("test-role"); // assertTrue("Role test-role was not removed",
	 * checkEntity("role", // "test-role")); }
	 */
}
