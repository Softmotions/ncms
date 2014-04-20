package com.softmotions.ncms.db.media;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlRow;
import com.google.inject.Inject;
import com.softmotions.commons.weboot.eb.WBEBeanModule;
import com.softmotions.ncms.NcmsWebTest;
import com.softmotions.ncms.media.model.MediaFile;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by shu on 4/18/2014.
 */
public class EbeanTest extends NcmsWebTest {

  @Inject
  WBEBeanModule.EbeanProvider ebeanProvider;

  EbeanServer ebean;

  @Before
  public void setup() {
    ebean = ebeanProvider.get();
  }

  @Test
  public void testEbeanDatasource() {
    //Model m;
    String sql = "select count(*) as count from dual";
    SqlRow row = ebean.createSqlQuery(sql).findUnique();
    Integer count = row.getInteger("count");
    //System.out.println("Count: " + count + " - DataSource is ok");
  }

  void checkName(long id, String name) {
    MediaFile mediaFile = ebean.find(MediaFile.class, id);
    assertNotNull(mediaFile);
    assertEquals(name, mediaFile.getName());
  }

  @Test
  public void testMediaFile() {
    List<MediaFile> list = ebean.find(MediaFile.class).findList();
    System.out.println("List: " + list.size());
    MediaFile mediaFile = new MediaFile();
    mediaFile.setName("test");
    mediaFile.setDescription("something");
    mediaFile.setFilePath("path");
    ebean.save(mediaFile);
    checkName(mediaFile.getId(), "test");

    mediaFile.setName("changed");
    ebean.update(mediaFile);
    checkName(mediaFile.getId(), "changed");

    ebean.delete(mediaFile);
    MediaFile mf = ebean.find(MediaFile.class, mediaFile.getId());
    assertNull(mf);
  }

}
