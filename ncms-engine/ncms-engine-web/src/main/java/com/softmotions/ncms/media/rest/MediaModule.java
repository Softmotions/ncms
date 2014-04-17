package com.softmotions.ncms.media.rest;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Created with IntelliJ IDEA.
 * User: shu
 * Date: 4/12/14
 * Time: 5:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class MediaModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(TestService.class).in(Singleton.class);
  }
}
