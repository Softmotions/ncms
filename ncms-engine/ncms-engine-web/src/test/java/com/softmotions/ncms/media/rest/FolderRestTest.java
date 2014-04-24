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

	private ResteasyWebTarget target(MediaFolder subFolder) {
		return getWebTarget("/" + subFolder.getId());
	}

	private Entity<MediaFolder> entity(MediaFolder folder) {
		return Entity.entity(folder, "application/json");
	}

	@Test
	public void testCreateDelete() throws Exception {
		MediaFolder folder = new MediaFolder("test-folder");
		folder.setDescription("test-desc");

		assertEquals(500, getWebTarget("/123").request().delete().getStatus());

		Response response = getWebTarget("/").request().post(entity(folder));
		assertEquals(200, response.getStatus());
		MediaFolder f = response.readEntity(MediaFolder.class);
		assertEquals(folder.getName(), f.getName());
		assertEquals(folder.getDescription(), f.getDescription());
		response.close();

		response = target(f).request().get();
		assertEquals(200, response.getStatus());
		f = response.readEntity(MediaFolder.class);
		assertEquals(folder.getName(), f.getName());
		assertEquals(folder.getDescription(), f.getDescription());
		response.close();

		response = target(f).request().post(entity(folder));
		assertEquals(200, response.getStatus());
		MediaFolder subFolder = response.readEntity(MediaFolder.class);
		assertEquals(folder.getName(), subFolder.getName());
		assertEquals(folder.getDescription(), subFolder.getDescription());
		response.close();

		response = target(subFolder).request().delete();
		assertEquals(200, response.getStatus());
		response.close();

		response = target(subFolder).request().get();
		assertEquals(404, response.getStatus());
		response.close();

		response = target(f).request().delete();
		assertEquals(200, response.getStatus());
		response.close();

		response = target(f).request().get();
		assertEquals(404, response.getStatus());
		response.close();
	}

	@Test
	public void testUpdate() throws Exception {
		MediaFolder folder = new MediaFolder("test-folder");
		folder.setDescription("test-desc");

		Response response = getWebTarget("/").request().post((entity(folder)));
		assertEquals(200, response.getStatus());
		MediaFolder created = response.readEntity(MediaFolder.class);
		assertEquals(folder.getName(), created.getName());
		assertEquals(folder.getDescription(), created.getDescription());
		response.close();

		response = target(created).request().get();
		assertEquals(200, response.getStatus());
		MediaFolder f1 = response.readEntity(MediaFolder.class);
		assertEquals(folder.getName(), f1.getName());
		assertEquals(folder.getDescription(), f1.getDescription());
		response.close();

		MediaFolder folder2 = new MediaFolder("test-folder2");
		folder2.setDescription("test-desc2");
		response = target(created).request().put(entity(folder2));
		MediaFolder f2 = response.readEntity(MediaFolder.class);
		assertEquals(200, response.getStatus());
		assertEquals(folder2.getName(), f2.getName());
		assertEquals(folder2.getDescription(), f2.getDescription());
		response.close();

		response = target(created).request().get();
		assertEquals(200, response.getStatus());
		MediaFolder f3 = response.readEntity(MediaFolder.class);
		assertEquals(folder2.getName(), f3.getName());
		assertEquals(folder2.getDescription(), f3.getDescription());
		response.close();

	}

}
