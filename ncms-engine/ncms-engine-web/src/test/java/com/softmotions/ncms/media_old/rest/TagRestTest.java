package com.softmotions.ncms.media_old.rest;

import com.google.inject.Inject;
import com.softmotions.ncms.media_old.db.MediaDataManager;
import com.softmotions.ncms.media_old.model.MediaFile;
import com.softmotions.ncms.media_old.model.MediaFolder;
import com.softmotions.ncms.media_old.model.Tag;
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

	@Test
	public void testFolderTags() throws Exception {
		Tag tag = createAndCheck(Tag.of("tag1"));
		requestAndCheck(tag);

		listTagsAndCheck(1);


		MediaFolder folder = createAndCheck(MediaFolder.of("f1", "f1d"));
		Response response = getWebTarget("/folder/" + folder.getId() + "/" + tag.getId()).request().get();
		assertEquals(200, response.getStatus());

		response = getWebTarget("/folder/" + folder.getId() + "/" + tag.getId()).request().get();
		assertEquals(500, response.getStatus());

		MediaFolder folder1 = requestAndCheck(folder);
		assertEquals(1, folder1.getTags().size());
		assertTrue(folder1.hasTag(tag));

		response = getWebTarget("/folder/" + folder.getId() + "/" + tag.getId()).request().delete();
		assertEquals(200, response.getStatus());

		response = getWebTarget("/folder/" + folder.getId() + "/" + tag.getId()).request().delete();
		assertEquals(500, response.getStatus());

		MediaFolder folder2 = requestAndCheck(folder);
		assertFalse(folder2.hasTag(tag));
	}

	@Test
	public void testFileTags() throws Exception {
		Tag tag = createAndCheck(Tag.of("tag1"));
		requestAndCheck(tag);

		listTagsAndCheck(1);

		MediaFolder root = createAndCheck(MediaFolder.of("root", "root-desc"));
		MediaFile file = createAndCheck(root, MediaFile.of("f1", "f1d"));

		Response response = getWebTarget("/file/" + file.getId() + "/" + tag.getId()).request().get();
		assertEquals(200, response.getStatus());

		response = getWebTarget("/file/" + file.getId() + "/" + tag.getId()).request().get();
		assertEquals(500, response.getStatus());

		MediaFile file1 = requestAndCheck(file);
		assertEquals(1, file1.getTags().size());
		assertTrue(file1.hasTag(tag));

		response = getWebTarget("/file/" + file.getId() + "/" + tag.getId()).request().delete();
		assertEquals(200, response.getStatus());

		response = getWebTarget("/file/" + file.getId() + "/" + tag.getId()).request().delete();
		assertEquals(500, response.getStatus());

		MediaFile file2 = requestAndCheck(file);
		assertFalse(file2.hasTag(tag));


	}

}
