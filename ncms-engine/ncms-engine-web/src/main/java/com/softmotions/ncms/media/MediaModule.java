package com.softmotions.ncms.media;

import com.softmotions.ncms.media.db.MediaDbModule;
import com.softmotions.ncms.media.rest.MediaRestModule;

import com.google.inject.AbstractModule;

/**
 * Created by shu on 4/19/2014.
 */
public class MediaModule extends AbstractModule {
  @Override
  protected void configure() {
		install(new MediaDbModule());
		install(new MediaRestModule());
  }
}
