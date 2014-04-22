package com.softmotions.ncms.db.media;

import com.avaje.ebean.EbeanServer;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.softmotions.ncms.NcmsWebTest;
import com.softmotions.ncms.media.db.MediaDbModule;
import com.softmotions.ncms.media.model.MediaFile;
import com.softmotions.ncms.media.model.MediaFolder;
import com.softmotions.ncms.media.model.Tag;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by shu on 4/22/2014.
 */
public class MediaFolderTest  extends NcmsWebTest {

	//@Inject
	EbeanServer ebean;

	@Inject
	MediaDbModule.EbeanServerProvider ebeanServerProvider;

	@Before
	public void setup() {
		ebean = ebeanServerProvider.get();
	}

	Tag tag(String name) {
		Tag tag = ebean.find(Tag.class).where().eq("name", name).findUnique();
		if(tag == null) {
			tag = Tag.of(name);
			//ebean.save(tag);
		}
		return tag;
	}

	@Test
	public void testMediaFolder() {
		MediaFolder mediaFolder1 = MediaTestUtils.createMediaFolder(1);
		ebean.save(mediaFolder1);

		MediaFolder mf1 = ebean.find(MediaFolder.class, mediaFolder1.getId());
		assertEquals("test-1", mf1.getName());
		assertEquals("something-1", mf1.getDescription());
	}

	@Test
	public void testMediaFolderFiles() {
		MediaFolder mediaFolder1 = MediaTestUtils.createMediaFolder(1);
		ebean.save(mediaFolder1);

		MediaFolder mf1 = ebean.find(MediaFolder.class, mediaFolder1.getId());

		MediaFile file1 = MediaTestUtils.createMediaFile(1);
		MediaFile file2 = MediaTestUtils.createMediaFile(2);

		ebean.save(file1);
		ebean.save(file2);

		mf1.addMediaFile(file1);
		mf1.addMediaFile(file2);

		ebean.update(file1);
		ebean.update(file2);

		List<MediaFile> files = ebean.find(MediaFile.class).where().eq("media_Folder_ID", mediaFolder1.getId()).findList();
		assertEquals(2, files.size());

		MediaFile f1 = files.get(0);
		//f1.setName("del");
		f1.setMediaFolder(null);
		ebean.update(f1);

		files = ebean.find(MediaFile.class).where().eq("media_Folder_ID", mediaFolder1.getId()).findList();
		assertEquals(1, files.size());

	}

	@Test
	public void testMediaFolderTagsAddDelete() {
		MediaFolder mediaFile1 = MediaTestUtils.createMediaFolder(1);
		mediaFile1.setTags(Lists.newArrayList(tag("aaa"), tag("bbb"), tag("ccc")));
		ebean.save(mediaFile1);

		MediaFolder mf1 = ebean.find(MediaFolder.class, mediaFile1.getId());
		List<Tag> tags = mf1.getTags();
		assertNotNull(tags);
		assertEquals(3, tags.size());
		assertTrue(tags.contains(tag("aaa")));
		assertTrue(tags.contains(tag("bbb")));
		assertTrue(tags.contains(tag("ccc")));
		assertTrue(!tags.contains(tag("xxx")));
		assertTrue(!tags.contains(tag("zzz")));

		assertTrue(mf1.hasTag(tag("aaa")));
		assertTrue(mf1.hasTag(tag("bbb")));
		assertTrue(mf1.hasTag(tag("ccc")));

		assertTrue(mf1.deleteTag(tag("bbb")));
		ebean.update(mf1);
		mf1 = ebean.find(MediaFolder.class, mediaFile1.getId());
		tags = mf1.getTags();
		assertNotNull(tags);
		assertEquals(2, tags.size());
		assertTrue(tags.contains(tag("aaa")));
		assertTrue(!tags.contains(tag("bbb")));
		assertTrue(tags.contains(tag("ccc")));
		assertTrue(!tags.contains(tag("xxx")));
		assertTrue(!tags.contains(tag("zzz")));
	}

}
