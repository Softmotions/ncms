package com.softmotions.ncms.media.rest;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.softmotions.ncms.media.model.MediaFile;
import com.softmotions.ncms.media.model.MediaFolder;
import com.softmotions.ncms.media.model.Tag;

/**
 * Created by shu on 4/19/2014.
 */
public class MediaRestModule extends AbstractModule {
  @Override
  protected void configure() {
	  bind(TestService.class).in(Singleton.class);
	  //bind(FolderRestController.class).in(Singleton.class);
	  //bind(FoldersRestController.class).in(Singleton.class);
	  //bind(FileRestController.class).in(Singleton.class);
  }
}
