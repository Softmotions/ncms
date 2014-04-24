package com.softmotions.ncms.media.rest;

import com.softmotions.ncms.media.db.MediaRestTest;
import com.softmotions.ncms.media.model.MediaFolder;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.jboss.resteasy.client.ClientRequest;


import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.junit.Assert.*;

/**
 * Created by shu on 4/24/2014.
 */
public class FolderRestTest extends MediaRestTest {

	public FolderRestTest() {
		super("/folder");
	}

	@Test
	public void testCreate() throws Exception {
		MediaFolder folder = new MediaFolder("test-folder");
		ResteasyWebTarget target = getWebTarget("/");
		folder.setDescription("test-desc");
		Response response = target.request().post((Entity.entity(folder, "application/json")));
		assertEquals(201, response.getStatus());
		response.close();
	}

}
