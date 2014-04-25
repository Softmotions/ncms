package com.softmotions.ncms.media.rest;

import com.google.inject.Inject;
import com.softmotions.ncms.media.db.MediaDataManager;
import com.softmotions.ncms.media.model.MediaFile;
import com.softmotions.ncms.media.model.MediaFolder;
import com.softmotions.ncms.media.model.Tag;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.*;

/**
 * Created by shu on 4/24/2014.
 */
public class TagRestTest extends MediaRestTestBase {

	@Inject
	MediaDataManager manager;

	public TagRestTest() {
		super("/tag");
	}

	@Test
	public void testCreateDelete() throws Exception {
		assertEquals(500, getWebTarget("/123").request().delete().getStatus());

		Tag tag = createAndCheck(Tag.of("tag1"));
		requestAndCheck(tag);

		listTagsAndCheck(1);

		Response response = target(tag).request().delete();
		assertEquals(200, response.getStatus());
		response.close();
		requestAndCheckNotExists(tag);

		listTagsAndCheck(0);

	}

	//@Test
	public void testUpdate() throws Exception {
		MediaFolder root = createAndCheck(MediaFolder.of("test-folder", "test-desc"));
		requestAndCheck(root);

		MediaFile file = createAndCheck(root, MediaFile.of("test-file", "test-desc"));
		requestAndCheck(file);

		MediaFile file2 = MediaFile.of("test-file2", "test-desc2");
		Response response = target(file).request().put(entity(file2));
		MediaFile f2 = response.readEntity(MediaFile.class);
		assertEquals(200, response.getStatus());
		assertEquals(file2.getName(), f2.getName());
		assertEquals(file2.getDescription(), f2.getDescription());
		response.close();

		requestAndCheck(f2);

	}

	//@Test
	public void testMoveFile() throws Exception {
		MediaFolder root1 = createAndCheck(MediaFolder.of("test-folder", "test-desc"));
		requestAndCheck(root1);
		MediaFolder root2 = createAndCheck(MediaFolder.of("test-folder2", "test-desc2"));
		requestAndCheck(root2);

		MediaFile file = createAndCheck(root1, MediaFile.of("test-file", "test-desc"));
		requestAndCheck(file);

		//checkFileInFolder();

		Response response = getWebTarget("/" + file.getId() + "/" + root2.getId()).request().get();
		assertEquals(200, response.getStatus());
		response.close();


		System.out.println("File: " + file + " " + file.getClass());
		System.out.println("ROOT2 Files:");
		for(MediaFile f: manager.getFiles(root2)) {
			System.out.println("  " + f + " eq " + file.equals(f) + " " + f.getClass());
		}
		assertFalse(manager.getFiles(root1).contains(file));
		assertTrue(manager.getFiles(root2).contains(file));

	}

}
