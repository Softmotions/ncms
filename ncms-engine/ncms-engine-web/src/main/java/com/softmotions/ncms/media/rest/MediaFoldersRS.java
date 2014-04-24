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
import java.util.Objects;

/**
 * Created by shu on 4/23/2014.
 */

/*
+ get 	/folders/show
+ get 	/folders
+ get 	/folders/id
+ get	/folders/id/id
*/
@Path("media/folders")
public class MediaFoldersRS {

	@Inject
	MediaDataManager manager;

	@Inject
	EbeanServer ebean;

	@GET
	@Path("/show")
	@Produces("text/plain")
	public String show() {
		return manager.dump(null, 0);
	}

	@GET
	@Path("/")
	@Produces("application/json")
	public Response getRootFolders() {
		List<MediaFolder> folders = manager.getRootFolders();
		return Response.status(200).entity(folders).build();
	}

	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response getSubSolders(@PathParam("id") Long id) {
		MediaFolder folder = ebean.find(MediaFolder.class, id);
		if(folder == null) {
			return Response.status(500).entity("Folder not found: " + id).build();
		}
		return Response.status(200).entity(manager.getSubFolders(folder)).build();
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
		if(Objects.equals(folder,host)) {
			return Response.status(500).entity("Couldn't move to itself: " + hostId).build();
		}
		if(folder.isParentOf(host)) {
			return Response.status(500).entity("Couldn't move to own subtree: " + hostId).build();
		}
		folder.setParent(host);
		ebean.update(folder);
		return Response.status(200).entity("moved: " + id).build();
	}

}
