package com.softmotions.ncms.media.rest;

import com.softmotions.ncms.media.db.MediaRestTest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;
import org.jboss.resteasy.client.ClientRequest;

import static org.junit.Assert.*;

/**
 * Created by shu on 4/24/2014.
 */
public class FolderRestTest extends MediaRestTest {

	@Test
	public void testResteasyClient() throws Exception {
		String address = getServerAddress();
		ClientRequest request = new ClientRequest(address + "/ncms/rs/media/folders/show");
		//request.accept("application/json");
		ClientResponse<String> response = request.get(String.class);
		assertEquals(200, response.getStatus());
		//ResteasyClient client = new ResteasyClientBuilder().build();
	}

	//@Test
	public void testCreateDelete() throws Exception {
		ClientRequest request = new ClientRequest("http://localhost:8080/RESTfulExample/json/product/get");
		//request.accept("application/json");
		ClientResponse<String> response = request.get(String.class);
		assertEquals(200, response.getStatus());
		//ResteasyClient client = new ResteasyClientBuilder().build();
	}

}
