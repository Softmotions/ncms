package com.softmotions.ncms.media.rest;

import com.avaje.ebean.EbeanServer;
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
public class FolderRestController {

	@Inject
	MediaDataManager manager;

	@Inject
	EbeanServer ebean;

	@GET
	@Path("/{id}")
	@Produces("application/json")
	public MediaFolder getFolder(@PathParam("id") Long id) {
		MediaFolder folder = ebean.find(MediaFolder.class, id);
		return folder;
	}

	@PUT
	@Path("/{id}")
	@Consumes("application/json")
	public Response putFolder(@PathParam("id") Long id, MediaFolder folder) {
		MediaFolder fd = ebean.find(MediaFolder.class, id);
		if(fd == null) {
			return Response.status(500).entity("Folder not found: " + id).build();
		}
		fd.setName(folder.getName());
		fd.setDescription(folder.getDescription());
		ebean.update(fd);
		return Response.status(200).entity("updated ok: " + fd).build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteFolder(@PathParam("id") Long id) {
		MediaFolder folder = ebean.find(MediaFolder.class, id);
		if(folder == null) {
			return Response.status(500).entity("Folder not found: " + id).build();
		}
		manager.delete(folder);
		return Response.status(200).entity("deleted: " + id).build();
	}

	@POST
	@Path("/{folderId}")
	@Consumes("application/json")
	public Response createFolder(@PathParam("folderId") Long folderId, MediaFolder folder) {
		MediaFolder host = ebean.find(MediaFolder.class, folderId);
		if(host == null) {
			return Response.status(500).entity("Folder not found: " + folderId).build();
		}
		folder.setParent(host);
		ebean.save(folder);
		return Response.status(201).entity("saved: " + folder).build();
	}

	@POST
	@Path("/")
	@Consumes("application/json")
	public Response createRootFolder(MediaFolder folder) {
		ebean.save(folder);
		return Response.status(201).entity("saved: " + folder).build();
	}

}
