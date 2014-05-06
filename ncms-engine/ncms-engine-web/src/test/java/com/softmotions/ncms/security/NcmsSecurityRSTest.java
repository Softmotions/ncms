package com.softmotions.ncms.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.math.NumberUtils;
import org.junit.Test;

import com.softmotions.ncms.NcmsWebTest;

/**
 * @author Savelev Dmitry (dd.savelev@gmail.com)
 */
public class NcmsSecurityRSTest extends NcmsWebTest {

	private static final String PREFIX_URI = "/ncms/rs/adm/security";

	public NcmsSecurityRSTest() {
	}

	@Test
	public void testAll() throws Exception {
		String address = getServerAddress();

		/* ------------ user --------------- */
		/* ------------ test user count ------------------- */
        WebTarget wt = ClientBuilder.newClient().target(
                address + PREFIX_URI + "/users/count");
        Response resp = wt.request().buildGet().invoke();
        long userCount = resp.readEntity(Long.class);
        assertEquals(200, resp.getStatus());

		/* ------------ test create user ------------------- */
		wt = ClientBuilder.newClient().target(
                address + PREFIX_URI + "/user/test-user");
		wt = wt.queryParam("password", "password").queryParam("fullname", "fullname");
		resp = wt.request().buildPost(null).invoke();
		String data = resp.readEntity(String.class);
		assertEquals(200, resp.getStatus());
        assertTrue(data.contains("test-user"));

		/* ------------ test user count ------------------- */
        wt = ClientBuilder.newClient().target(
                address + PREFIX_URI + "/users/count");
        resp = wt.request().buildGet().invoke();
        userCount = resp.readEntity(Long.class) - userCount;
        assertEquals(200, resp.getStatus());
        assertTrue(userCount == 1);

		/* ------------ test get user ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + PREFIX_URI + "/user/test-user");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		assertEquals(200, resp.getStatus());
        assertTrue(data.contains("test-user"));

		/* ------------ test find user ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + PREFIX_URI + "/user/test-user");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		assertEquals(200, resp.getStatus());
        assertTrue(data.contains("test-user"));

		/* ------------ test user count ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + PREFIX_URI + "/users/count");
		resp = wt.request().buildGet().invoke();
		userCount = resp.readEntity(Long.class);
		assertEquals(200, resp.getStatus());
        assertTrue(userCount > 1);

		/* ------------ test users ------------------- */
		wt = ClientBuilder.newClient().target(address + PREFIX_URI + "/users");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		assertEquals(200, resp.getStatus());
        assertTrue(data.contains("test-user"));

		/* ------------ test users ------------------- */
		wt = ClientBuilder.newClient().target(address + PREFIX_URI + "/users");
		wt = wt.queryParam("firstRow", 0).queryParam("lastRow", userCount);
		resp = wt.request()
		        .buildPost(Entity.entity(String.class, MediaType.TEXT_PLAIN))
		        .invoke();
		data = resp.readEntity(String.class);
		assertEquals(200, resp.getStatus());
        assertTrue(data.contains("test-user"));

		/* ------------ test delete user ------------------- */
		wt = ClientBuilder.newClient().target(
                address + PREFIX_URI + "/user/test-user");
		resp = wt.request().buildDelete().invoke();
		assertEquals(204, resp.getStatus());

        /* ------------ test user count ------------------- */
        wt = ClientBuilder.newClient().target(
                address + PREFIX_URI + "/users/count");
        resp = wt.request().buildGet().invoke();
        userCount =  userCount - resp.readEntity(Long.class);
        assertEquals(200, resp.getStatus());
        assertTrue(userCount == 1);

		/* ------------ role --------------- */
		/* ------------ test roles ------------------- */
        wt = ClientBuilder.newClient().target(address + PREFIX_URI + "/roles");
        resp = wt.request().buildGet().invoke();
        data = resp.readEntity(String.class);
        assertEquals(200, resp.getStatus());
        assertTrue(!data.contains("test-role"));

		/* ------------ test create role ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + PREFIX_URI + "/role/test-role");
		wt = wt.queryParam("description", "test-role-description");
		resp = wt.request()
		        .buildPost(Entity.entity(String.class, MediaType.TEXT_PLAIN))
		        .invoke();
		data = resp.readEntity(String.class);
		assertEquals(200, resp.getStatus());
        assertTrue(data.contains("test-role"));

		/* ------------ test roles ------------------- */
        wt = ClientBuilder.newClient().target(address + PREFIX_URI + "/roles");
        resp = wt.request().buildGet().invoke();
        data = resp.readEntity(String.class);
        assertEquals(200, resp.getStatus());
        assertTrue(data.contains("test-role"));

		/* ------------ test get role ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + PREFIX_URI + "/role/test-role");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		assertEquals(200, resp.getStatus());
        assertTrue(data.contains("test-role"));

		/* ------------ test find role ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + PREFIX_URI + "/role/test-role");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		assertEquals(200, resp.getStatus());
        assertTrue(data.contains("test-role"));

		/* ------------ test delete role ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + PREFIX_URI + "/role/test-role");
		resp = wt.request().buildDelete().invoke();
		assertEquals(204, resp.getStatus());

		/* ------------ test roles ------------------- */
        wt = ClientBuilder.newClient().target(address + PREFIX_URI + "/roles");
        resp = wt.request().buildGet().invoke();
        data = resp.readEntity(String.class);
        assertEquals(200, resp.getStatus());
        assertTrue(!data.contains("test-role"));

		/* ------------ group --------------- */
		/* ------------ test groups ------------------- */
        wt = ClientBuilder.newClient().target(address + PREFIX_URI + "/groups");
        resp = wt.request().buildGet().invoke();
        data = resp.readEntity(String.class);
        assertEquals(200, resp.getStatus());
        assertTrue(!data.contains("test-group"));

		/* ------------ test create group ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + PREFIX_URI + "/group/test-group");
		wt = wt.queryParam("description", "test-group-description");
		resp = wt.request()
		        .buildPost(Entity.entity(String.class, MediaType.TEXT_PLAIN))
		        .invoke();
		data = resp.readEntity(String.class);
		assertEquals(200, resp.getStatus());
        assertTrue(data.contains("test-group"));

		/* ------------ test groups ------------------- */
        wt = ClientBuilder.newClient().target(address + PREFIX_URI + "/groups");
        resp = wt.request().buildGet().invoke();
        data = resp.readEntity(String.class);
        assertEquals(200, resp.getStatus());
        assertTrue(data.contains("test-group"));

		/* ------------ test find group ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + PREFIX_URI + "/group/test-group");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		assertEquals(200, resp.getStatus());
        assertTrue(data.contains("test-group"));

		/* ------------ test groups ------------------- */
		wt = ClientBuilder.newClient().target(address + PREFIX_URI + "/groups");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		assertEquals(200, resp.getStatus());
        assertTrue(data.contains("test-group"));

		/* ------------ test delete group ------------------- */
		wt = ClientBuilder.newClient().target(
		        address + PREFIX_URI + "/group/test-group");
		resp = wt.request().buildDelete().invoke();
		assertEquals(204, resp.getStatus());

		/* ------------ test groups ------------------- */
		wt = ClientBuilder.newClient().target(address + PREFIX_URI + "/groups");
		resp = wt.request().buildGet().invoke();
		data = resp.readEntity(String.class);
		assertEquals(200, resp.getStatus());
        assertTrue(!data.contains("test-group"));
	}
}
