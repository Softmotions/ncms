package com.softmotions.ncms.media.rest;

import com.softmotions.ncms.media.model.MediaFolder;
import org.junit.Test;


import javax.ws.rs.core.Response;

import static org.junit.Assert.*;

/**
 * Created by shu on 4/24/2014.
 */
public class FolderRestTest extends FoldersRestTestBase {

	public FolderRestTest() {
		super("/folder");
	}

	@Test
	public void testCreateDelete() throws Exception {
		assertEquals(500, getWebTarget("/123").request().delete().getStatus());

		MediaFolder root = createAndCheck(MediaFolder.of("test-folder", "test-desc"));
		requestAndCheck(root);

		MediaFolder subFolder = createAndCheck(root, MediaFolder.of("test-sub-folder", "test-sub-desc"));
		requestAndCheck(subFolder);

		Response response = target(subFolder).request().delete();
		assertEquals(200, response.getStatus());
		response.close();

		response = target(subFolder).request().get();
		assertEquals(404, response.getStatus());
		response.close();

		response = target(root).request().delete();
		assertEquals(200, response.getStatus());
		response.close();

		response = target(root).request().get();
		assertEquals(404, response.getStatus());
		response.close();
	}

	@Test
	public void testUpdate() throws Exception {
		MediaFolder root = createAndCheck(MediaFolder.of("test-fo", "test-desc"));
		requestAndCheck(root);

		MediaFolder folder2 = MediaFolder.of("test-folder2", "test-desc2");
		Response response = target(root).request().put(entity(folder2));
		MediaFolder f2 = response.readEntity(MediaFolder.class);
		assertEquals(200, response.getStatus());
		assertEquals(folder2.getName(), f2.getName());
		assertEquals(folder2.getDescription(), f2.getDescription());
		response.close();

		response = target(root).request().get();
		assertEquals(200, response.getStatus());
		MediaFolder f3 = response.readEntity(MediaFolder.class);
		assertEquals(folder2.getName(), f3.getName());
		assertEquals(folder2.getDescription(), f3.getDescription());
		response.close();

	}

}
