package com.softmotions.ncms.media.rest;

import com.avaje.ebean.EbeanServer;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.softmotions.ncms.media.db.MediaDataManager;
import com.softmotions.ncms.media.model.MediaFolder;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by shu on 4/23/2014.
 */

/*
+ get 	  /folder/id
+ put 	  /folder/id
+ delete	/folder/id
+ post 	  /folder/id
+ post	  /folder
*/

@Path("media/folder")
public class MediaFolderRS {

	@Inject
	MediaDataManager manager;

	@Inject
	EbeanServer ebean;

	protected Response response(int code, Object entity) {
		return Response.status(code).entity(entity).build();
	}

	protected Response ok(Object entity) {
		return response(200, entity);
	}

	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response getFolder(@PathParam("id") Long id) {
		MediaFolder folder = ebean.find(MediaFolder.class, id);
		if(folder == null) {
			return response(404, "Folder not found: " + id);
		}
		return ok(folder);
	}

	@PUT
	@Path("/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response putFolder(@PathParam("id") Long id, MediaFolder folder) {
		MediaFolder fd = ebean.find(MediaFolder.class, id);
		if(fd == null) {
			return response(500, "Folder not found: " + id);
		}
		fd.setName(folder.getName());
		fd.setDescription(folder.getDescription());
		ebean.update(fd);
		return ok(fd);
	}

	@DELETE
	@Path("/{id}")
	public Response deleteFolder(@PathParam("id") Long id) {
		MediaFolder folder = ebean.find(MediaFolder.class, id);
		if(folder == null) {
			return response(500, "Folder not found: " + id);
		}
		manager.delete(folder);
		return ok("deleted: " + id);
	}

	@POST
	@Path("/{folderId}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response createFolder(@PathParam("folderId") Long folderId, MediaFolder folder) {
		MediaFolder host = ebean.find(MediaFolder.class, folderId);
		if(host == null) return response(500, "Folder not found: " + folderId);
		folder.setParent(host);
		ebean.save(folder);
		return ok(folder);
	}

	@POST
	@Path("/")
	@Consumes("application/json")
	@Produces("application/json")
	public Response createRootFolder(MediaFolder folder) {
		ebean.save(folder);
		return ok(folder);
	}

}
