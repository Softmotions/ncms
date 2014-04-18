package com.softmotions.ncms.db.media;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.softmotions.ncms.media.model.MediaFile;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by shu on 4/18/2014.
 */
public class EbeanTest {

  @Test
  public void testEbeanDatasource() {
    //Model m;
    String sql = "select count(*) as count from dual";
    SqlRow row = Ebean.createSqlQuery(sql).findUnique();
    Integer count = row.getInteger("count");
    //System.out.println("Count: " + count + " - DataSource is ok");
  }

  void checkName(long id, String name) {
    MediaFile mediaFile = Ebean.find(MediaFile.class, id);
    assertNotNull(mediaFile);
    assertEquals(name, mediaFile.getName());
  }

  @Test
  public void testMediaFile() {
    MediaFile mediaFile = new MediaFile();
    mediaFile.setName("test");
    mediaFile.setDescription("something");
    Ebean.save(mediaFile);
    checkName(mediaFile.getId(), "test");

    mediaFile.setName("changed");
    Ebean.save(mediaFile);
    checkName(mediaFile.getId(), "changed");

    Ebean.delete(mediaFile);
    MediaFile mf = Ebean.find(MediaFile.class, mediaFile.getId());
    assertNull(mf);
  }

}
