package com.softmotions.ncms.media.db;

import com.softmotions.ncms.media.model.MediaFile;
import com.softmotions.ncms.media.model.MediaFolder;
import com.softmotions.ncms.media.model.Tag;

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
