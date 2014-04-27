package com.softmotions.ncms.media.rest;

import com.softmotions.ncms.media.model.MediaFolder;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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
public class MediaFolderRS extends MediaRestBase {

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