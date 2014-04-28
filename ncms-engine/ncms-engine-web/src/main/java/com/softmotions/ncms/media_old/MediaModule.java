package com.softmotions.ncms.media_old;

import com.softmotions.ncms.media_old.db.MediaDbModule;
import com.softmotions.ncms.media_old.rest.MediaRestModule;

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
