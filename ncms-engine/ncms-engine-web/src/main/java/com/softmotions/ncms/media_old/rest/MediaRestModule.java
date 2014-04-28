package com.softmotions.ncms.media_old.rest;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Created by shu on 4/19/2014.
 */
public class MediaRestModule extends AbstractModule {
  @Override
  protected void configure() {
	  bind(MediaFolderRS.class).in(Singleton.class);
	  bind(MediaFoldersRS.class).in(Singleton.class);
	  bind(MediaFileRS.class).in(Singleton.class);
	  bind(MediaFilesRS.class).in(Singleton.class);
	  bind(TagRS.class).in(Singleton.class);
	  bind(FileUploadRS.class).in(Singleton.class);
  }
}
