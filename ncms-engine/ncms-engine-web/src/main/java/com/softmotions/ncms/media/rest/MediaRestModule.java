package com.softmotions.ncms.media.rest;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Created by shu on 4/19/2014.
 */
public class MediaRestModule extends AbstractModule {
  @Override
  protected void configure() {
	  bind(TestService.class).in(Singleton.class);
	  bind(MediaFolderRS.class).in(Singleton.class);
	  bind(MediaFoldersRS.class).in(Singleton.class);
	  bind(MediaFileRS.class).in(Singleton.class);
  }
}
