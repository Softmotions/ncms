package com.softmotions.ncms.db.media;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlRow;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.softmotions.ncms.NcmsWebTest;
import com.softmotions.ncms.media.db.MediaDbModule;
import com.softmotions.ncms.media.model.MediaFile;
import com.softmotions.ncms.media.model.Tag;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by shu on 4/18/2014.
 */
public class MediaFileTest extends NcmsWebTest {

  //@Inject
  EbeanServer ebean;

	@Inject
	MediaDbModule.EbeanServerProvider ebeanServerProvider;

	@Before
	public void setup() {
		ebean = ebeanServerProvider.get();
	}

  //@Test
  public void testEbeanDatasource() {
    String sql = "select count(*) as count from dual";
    SqlRow row = ebean.createSqlQuery(sql).findUnique();
    Integer count = row.getInteger("count");
    //System.out.println("Count: " + count + " - DataSource is ok");
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
	public void testMediaFile() {
		MediaFile mediaFile1 = MediaTestUtils.createMediaFile(1);
		ebean.save(mediaFile1);

		MediaFile mf1 = ebean.find(MediaFile.class, mediaFile1.getId());
		assertNotNull(mf1);
		assertEquals("test-1", mf1.getName());
		List<Tag> tags = mf1.getTags();
		assertNotNull(tags);

		mediaFile1.setName("changed");
		ebean.update(mediaFile1);
		mf1 = ebean.find(MediaFile.class, mediaFile1.getId());
		assertNotNull(mediaFile1);
		assertEquals("changed", mediaFile1.getName());

		ebean.delete(mediaFile1);
		mf1 = ebean.find(MediaFile.class, mediaFile1.getId());
		assertNull(mf1);
	}

  @Test
  public void testMediaFileTagsOverlap() {
	  MediaFile mediaFile1 = MediaTestUtils.createMediaFile(1);
    mediaFile1.setTags(Lists.newArrayList(tag("aaa"), tag("bbb"), tag("ccc")));
    ebean.save(mediaFile1);

	  MediaFile mediaFile2 = MediaTestUtils.createMediaFile(2);
    mediaFile2.setTags(Lists.newArrayList(tag("aaa"), tag("xxx"), tag("zzz")));
    ebean.save(mediaFile2);

	  MediaFile mediaFile3 = MediaTestUtils.createMediaFile(3);
	  ebean.save(mediaFile3);
	  mediaFile3.addTag(tag("aaa"));
	  mediaFile3.addTag(tag("bbb"));
	  mediaFile3.addTag(tag("zzz"));
	  ebean.update(mediaFile3);

    MediaFile mf1 = ebean.find(MediaFile.class, mediaFile1.getId());
	  List<Tag> tags = mf1.getTags();
    assertNotNull(tags);
    assertEquals(3, tags.size());
    assertTrue(tags.contains(tag("aaa")));
    assertTrue(tags.contains(tag("bbb")));
    assertTrue(tags.contains(tag("ccc")));
    assertTrue(!tags.contains(tag("xxx")));
    assertTrue(!tags.contains(tag("zzz")));

	  MediaFile mf2 = ebean.find(MediaFile.class, mediaFile2.getId());
	  tags = mf2.getTags();
	  assertNotNull(tags);
	  assertEquals(3, tags.size());
	  assertTrue(tags.contains(tag("aaa")));
	  assertTrue(!tags.contains(tag("bbb")));
	  assertTrue(!tags.contains(tag("ccc")));
	  assertTrue(tags.contains(tag("xxx")));
	  assertTrue(tags.contains(tag("zzz")));

	  MediaFile mf3 = ebean.find(MediaFile.class, mediaFile3.getId());
	  assertNotNull(mf3);
	  tags = mf3.getTags();
	  assertNotNull(tags);
	  assertEquals(3, tags.size());
	  assertTrue(tags.contains(tag("aaa")));
	  assertTrue(tags.contains(tag("bbb")));
	  assertTrue(!tags.contains(tag("ccc")));
	  assertTrue(!tags.contains(tag("xxx")));
	  assertTrue(tags.contains(tag("zzz")));
  }

	@Test
	public void testMediaFileTagsAddDelete() {
		MediaFile mediaFile1 = MediaTestUtils.createMediaFile(1);
		mediaFile1.setTags(Lists.newArrayList(tag("aaa"), tag("bbb"), tag("ccc")));
		ebean.save(mediaFile1);

		MediaFile mf1 = ebean.find(MediaFile.class, mediaFile1.getId());
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
		mf1 = ebean.find(MediaFile.class, mediaFile1.getId());
		tags = mf1.getTags();
		assertNotNull(tags);
		assertEquals(2, tags.size());
		assertTrue(tags.contains(tag("aaa")));
		assertTrue(!tags.contains(tag("bbb")));
		assertTrue(tags.contains(tag("ccc")));
		assertTrue(!tags.contains(tag("xxx")));
		assertTrue(!tags.contains(tag("zzz")));
	}

	@Test
	public void testTagsEqual() {
		Tag t1 = tag("abc");
		Tag t2 = tag("abc");
		Tag t3 = tag("abc");
		assertEquals(t1, t2);
		assertEquals(t1, t3);
		assertEquals(t2, t3);
	}

}
