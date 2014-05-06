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
		wt = wt.queryParam("password", "password").queryParam(
		        "fullname", "fullname");
		Response resp = wt.request().buildPost(null).invoke();
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
		wt = wt.queryParam("firstRow", "0").queryParam("lastRow", "20");
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
		wt = wt.queryParam("description", "test-role-description");
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
		wt = wt.queryParam("description", "test-group-description");
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
}
