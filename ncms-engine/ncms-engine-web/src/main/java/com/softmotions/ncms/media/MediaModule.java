package com.softmotions.ncms.media;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.softmotions.ncms.media.db.MediaDbModule;
import com.softmotions.ncms.media.rest.*;

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
