package com.softmotions.ncms.media.db;

import com.google.inject.AbstractModule;
import com.softmotions.ncms.media.model.MediaFile;
import com.softmotions.ncms.media.model.Tag;

/**
 * Created by shu on 4/19/2014.
 */
public class MediaDbModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(MediaFile.class);
    bind(Tag.class);
  }
}
