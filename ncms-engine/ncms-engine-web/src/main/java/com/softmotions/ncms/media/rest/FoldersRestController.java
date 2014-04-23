package com.softmotions.ncms.media.rest;

import com.avaje.ebean.EbeanServer;
import com.google.inject.Inject;
import com.softmotions.ncms.media.db.MediaDataManager;
import com.softmotions.ncms.media.model.MediaFolder;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Created by shu on 4/23/2014.
 */

/*
+ get 	/folders
+ get 	/folders/id
+ put	/folders/id/id
*/
@Path("media/folders")
public class FoldersRestController {

	@Inject
	MediaDataManager manager;

	@Inject
	EbeanServer ebean;

	@GET
	@Path("/")
	public String getRootFolders() {
		List<MediaFolder> folders = manager.getRootFolders();
		return manager.dump(null, 0);
	}

	@GET
	@Path("/{id}")
	public String getSubSolders(@PathParam("id") Long id) {
		MediaFolder folder = ebean.find(MediaFolder.class, id);
		return manager.dump(folder, 0);
	}

	@PUT
	@Path("/{folderId}/{hostId}")
	public String moveFolder(@PathParam("folderId") Long folderId, @PathParam("hostId") Long hostId) {
		MediaFolder folder = ebean.find(MediaFolder.class, folderId);
		MediaFolder host = ebean.find(MediaFolder.class, hostId);
		folder.setParent(host);
		ebean.update(folder);
		return manager.dump(null, 0);
	}

}
