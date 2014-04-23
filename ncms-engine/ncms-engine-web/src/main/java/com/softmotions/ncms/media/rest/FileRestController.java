package com.softmotions.ncms.media.rest;

import com.avaje.ebean.EbeanServer;
import com.google.inject.Inject;
import com.softmotions.ncms.media.db.MediaDataManager;
import com.softmotions.ncms.media.model.MediaFolder;

import javax.ws.rs.*;

/**
 * Created by shu on 4/23/2014.
 */

/*
- get 	  /file/id
- put	    /file/id
- delete	/file/id
- post 	  /file/id
- put	    /file/id/id
*/

@Path("media/file")
public class FileRestController {

	@Inject
	MediaDataManager manager;

	@Inject
	EbeanServer ebean;

	@GET
	@Path("/{id}")
	public String getFolder(@PathParam("id") Long id) {
		MediaFolder folder = ebean.find(MediaFolder.class, id);
		return folder.toString();
	}

	@PUT
	@Path("/{id}")
	public String putFolder(@PathParam("id") Long id) {
		MediaFolder folder = ebean.find(MediaFolder.class, id);
		return folder.toString();
	}

	@DELETE
	@Path("/{id}")
	public String deleteFolder(@PathParam("id") Long id) {
		MediaFolder folder = ebean.find(MediaFolder.class, id);
		return folder.toString();
	}

	@POST
	@Path("/{folderId}")
	public String getSubSolders(@PathParam("folderId") Long folderId) {
		return manager.dump(null, 0);
	}

	@PUT
	@Path("/{id}/{folderId}")
	public String getSubSolders(@PathParam("id") Long id, @PathParam("folderId") Long folderId) {
		return manager.dump(null, 0);
	}

}
