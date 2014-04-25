package com.softmotions.ncms.media.rest;

import com.softmotions.ncms.media.model.MediaFile;
import com.softmotions.ncms.media.model.Tag;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by shu on 4/25/2014.
 */

/*
 + post   /tag
 + get    /tag
 + get    /tag/id
 + delete /tag/id

   put    /tag/file/id/id
   put    /tag/folder/id/id
   delete /tag/file/id/id
   delete /tag/folder/id/id

*/

@Path("media/tag")
public class TagRS extends MediaRestBase {

	@POST
	@Path("/")
	@Consumes("application/json")
	@Produces("application/json")
	public Response createTag(Tag tag) {
		ebean.save(tag);
		return ok(tag);
	}

	@GET
	@Path("/")
	@Produces("application/json")
	public Response getTags() {
		List<Tag> tags = ebean.find(Tag.class).findList();
		return ok(tags);
	}

	@DELETE
	@Path("/{id}")
	public Response deleteTag(@PathParam("id") Long id) {
		Tag tag = ebean.find(Tag.class, id);
		if(tag == null) response(500, "Tag not found: " + id);
		ebean.delete(tag);
		return ok("deleted: " + id);
	}

	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response getTag(@PathParam("id") Long id) {
		Tag tag = ebean.find(Tag.class, id);
		System.out.println("GET TAG: " + id + ": " + tag);
		if(tag == null) return response(404, "Tag not found: " + id);
		return ok(tag);
	}

}
