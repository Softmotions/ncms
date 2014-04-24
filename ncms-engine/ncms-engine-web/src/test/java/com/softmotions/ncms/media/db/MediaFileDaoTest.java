package com.softmotions.ncms.media.db;

import com.google.inject.Inject;
import com.softmotions.ncms.NcmsWebTest;
import com.softmotions.ncms.media.model.MediaFile;
import com.softmotions.ncms.media.model.MediaFileDao;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by shu on 4/17/2014.
 */
public class MediaFileDaoTest extends NcmsWebTest {

  @Inject
  MediaFileDao mediaFileDao;

  //@Test
  public void testMediaFileDao() {
    assertNotNull(mediaFileDao);
    List<MediaFile> list = mediaFileDao.selectAll();
    System.out.println("SIZE: " + list.size());

    mediaFileDao.deleteAll();
    list = mediaFileDao.selectAll();
    assertEquals(0, list.size());

    MediaFile mediaFile = new MediaFile();
    mediaFile.setName("name");
    mediaFile.setDescription("description");
    mediaFile.setFilePath("filePath");
    mediaFileDao.insert(mediaFile);

    list = mediaFileDao.selectAll();
    assertEquals(1, list.size());

    MediaFile mf = list.get(0);
    assertEquals(mediaFile, mf);

    mf = mediaFileDao.getById(mf.getId());
    assertEquals(mediaFile, mf);

    mf.setName("xxx");
    mediaFileDao.update(mf);

    mf = mediaFileDao.getById(mf.getId());
    assertNotEquals(mediaFile, mf);
    assertEquals("xxx", mf.getName());

    //mediaFileDao.deleteById(createMediaFile.getId());
    list = mediaFileDao.selectAll();
    assertEquals(0, list.size());

  }


}
