package com.softmotions.ncms.media.rest;

import com.avaje.ebean.EbeanServer;
import com.google.inject.Inject;
import com.softmotions.ncms.media.db.MediaDataManager;
import com.softmotions.ncms.media.model.MediaFile;
import com.softmotions.ncms.media.model.MediaFolder;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * Created by shu on 4/23/2014.
 */

/*
+ get 	  /file/id
+ put	    /file/id
+ delete	/file/id
+ post 	  /file/id
- put	    /file/id/id
*/

@Path("media/file")
public class MediaFileRS extends MediaRestBase {

	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response getFile(@PathParam("id") Long id) {
		MediaFile file = ebean.find(MediaFile.class, id);
		if(file == null) return response(404, "File not found: " + id);
		return ok(file);
	}

	@PUT
	@Path("/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response putFile(@PathParam("id") Long id, MediaFile file) {
		MediaFile f = ebean.find(MediaFile.class, id);
		if(f == null) return response(500, "File not found: " + id);
		f.setName(file.getName());
		f.setDescription(file.getDescription());
		ebean.update(f);
		return ok(f);
	}

	@DELETE
	@Path("/{id}")
	public Response deleteFile(@PathParam("id") Long id) {
		MediaFile f = ebean.find(MediaFile.class, id);
		if(f == null) return response(500, "File not found: " + id);
		ebean.delete(f);
		return ok("deleted: " + id);
	}

	@POST
	@Path("/{folderId}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response createFile(@PathParam("folderId") Long folderId, MediaFile file) {
		MediaFolder folder = ebean.find(MediaFolder.class, folderId);
		if(folder == null) return response(500, "Folder not found: " + folderId);
		file.setMediaFolder(folder);
		ebean.save(file);
		return ok(file);
	}

	@GET
	@Path("/{id}/{folderId}")
	public Response moveToFolder(@PathParam("id") Long id, @PathParam("folderId") Long folderId) {
		MediaFile f = ebean.find(MediaFile.class, id);
		if(f == null) return response(500, "File not found: " + id);
		MediaFolder folder = ebean.find(MediaFolder.class, folderId);
		if(folder == null) return response(500, "Folder not found: " + folderId);
		f.setMediaFolder(folder);
		ebean.update(f);
		return ok("moved to flder: " + folderId);
	}

}
