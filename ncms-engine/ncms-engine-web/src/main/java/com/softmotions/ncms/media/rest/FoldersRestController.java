package com.softmotions.ncms.media.rest;

import com.avaje.ebean.EbeanServer;
import com.google.inject.Inject;
import com.softmotions.ncms.media.db.MediaDataManager;
import com.softmotions.ncms.media.model.MediaFolder;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Created by shu on 4/23/2014.
 */

/*
+ get 	/folders
+ get 	/folders/id
+ get	/folders/id/id
*/
@Path("media/folders")
public class FoldersRestController {

	@Inject
	MediaDataManager manager;

	@Inject
	EbeanServer ebean;

	@GET
	@Path("/show")
	@Produces("text/plain")
	public String show() {
		List<MediaFolder> folders = manager.getRootFolders();
		return manager.dump(null, 0);
	}

	@GET
	@Path("/")
	@Produces("application/json")
	public List<MediaFolder> getRootFolders() {
		List<MediaFolder> folders = manager.getRootFolders();
		return folders;
	}

	@GET
	@Path("/{id}")
	public List<MediaFolder> getSubSolders(@PathParam("id") Long id) {
		MediaFolder folder = ebean.find(MediaFolder.class, id);
		if(folder == null) {
			throw new RuntimeException("Folder not found: " + id);
		}
		return manager.getSubFolders(folder);
	}

	@GET
	@Path("/{id}/{hostId}")
	public Response moveFolder(@PathParam("id") Long id, @PathParam("hostId") Long hostId) {
		MediaFolder folder = ebean.find(MediaFolder.class, id);
		if(folder == null) {
			return Response.status(500).entity("Folder not found: " + id).build();
		}
		MediaFolder host = ebean.find(MediaFolder.class, hostId);
		if(folder == null) {
			return Response.status(500).entity("Folder not found: " + hostId).build();
		}
		folder.setParent(host);
		ebean.update(folder);
		return Response.status(200).entity("moved: " + id).build();
	}

}
