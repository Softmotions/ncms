package com.softmotions.ncms.media.rest;

import com.softmotions.ncms.media.model.MediaFolder;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

/**
 * Created by shu on 4/24/2014.
 */
public class FoldersRestTest extends MediaRestTestBase {

	public FoldersRestTest() {
		super("/folders");
	}

	@Test
	public void testCreateHierarchy() throws Exception {
		MediaFolder root1 = createAndCheck(MediaFolder.of("root1", "desc-root1"));
		requestAndCheck(root1);
		MediaFolder root2 = createAndCheck(MediaFolder.of("root2", "desc-root2"));
		requestAndCheck(root2);

		MediaFolder sub11 = createAndCheck(root1, MediaFolder.of("sub11", "sub11-desc"));
		MediaFolder sub12 = createAndCheck(root1, MediaFolder.of("sub12", "sub12-desc"));
		requestAndCheck(sub11);
		requestAndCheck(sub12);

		MediaFolder sub21 = createAndCheck(root2, MediaFolder.of("sub21", "sub21-desc"));
		MediaFolder sub22 = createAndCheck(root2, MediaFolder.of("sub22", "sub22-desc"));
		MediaFolder sub23 = createAndCheck(root2, MediaFolder.of("sub23", "sub23-desc"));
		requestAndCheck(sub21);
		requestAndCheck(sub22);
		requestAndCheck(sub23);

		listFoldersAndCheck(2);
		listFoldersAndCheck(root1, 2);
		listFoldersAndCheck(root2, 3);
		listFoldersAndCheck(sub11, 0);

		Response response = getWebTarget("/folder", "/" + sub12.getId()).request().delete();
		assertEquals(200, response.getStatus());
		response.close();
		requestAndCheckNotExists(sub12);

		listFoldersAndCheck(root1, 1);

		response = getWebTarget("/" + sub11.getId() + "/" + root2.getId()).request().get();
		assertEquals(200, response.getStatus());
		response.close();

		listFoldersAndCheck(root1, 0);
		listFoldersAndCheck(root2, 4);

		response = getWebTarget("/folder", "/" + root2.getId()).request().delete();
		assertEquals(200, response.getStatus());
		response.close();

		requestAndCheckNotExists(root2);
		requestAndCheckNotExists(sub11);
		requestAndCheckNotExists(sub12);
		requestAndCheckNotExists(sub21);
		requestAndCheckNotExists(sub22);
		requestAndCheckNotExists(sub23);

		listFoldersAndCheck(1);

	}

}
