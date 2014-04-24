package com.softmotions.ncms.media.rest;

import com.softmotions.ncms.media.db.MediaRestTest;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

/**
 * Created by shu on 4/24/2014.
 */
public class FoldersRestTest extends MediaRestTest {

	public FoldersRestTest() {
		super("/folders");
	}

	@Test
	public void testResteasyRequest() throws Exception {
		String address = getServerAddress();
		ClientRequest request = new ClientRequest(address + "/ncms/rs/media/folders/show");
		ClientResponse<String> response = request.get(String.class);
		assertEquals(200, response.getStatus());
	}

	@Test
	public void testResteasyClient() throws Exception {
		ResteasyWebTarget target = getWebTarget("/show");//client.target(address + "/ncms/rs/media/folders/show");
		Response response = target.request().get();
		assertEquals(200, response.getStatus());
		response.close();
	}

}
