package com.softmotions.ncms.media.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Created with IntelliJ IDEA.
 * User: shu
 * Date: 4/12/14
 * Time: 5:05 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("adm/test")
public class TestService {

  @GET
  @Path("/{name}")
  public String hello(@PathParam("name") final String name) {
    return "Test  " + name;
  }
}