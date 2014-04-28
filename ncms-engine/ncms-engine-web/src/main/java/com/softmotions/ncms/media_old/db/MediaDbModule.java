package com.softmotions.ncms.media_old.db;

import com.softmotions.ncms.media_old.model.MediaFile;
import com.softmotions.ncms.media_old.model.MediaFolder;
import com.softmotions.ncms.media_old.model.Tag;

import com.google.inject.AbstractModule;

/**
 * Created by shu on 4/19/2014.
 */
public class MediaDbModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(MediaFile.class);
    bind(MediaFolder.class);
    bind(Tag.class);
  }
}
