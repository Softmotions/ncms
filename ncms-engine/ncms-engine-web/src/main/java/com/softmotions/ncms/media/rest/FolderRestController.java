package com.softmotions.ncms.media.rest;

import com.avaje.ebean.EbeanServer;
import com.google.inject.Inject;
import com.softmotions.ncms.media.db.MediaDataManager;
import com.softmotions.ncms.media.model.MediaFolder;

import javax.ws.rs.*;
import java.util.List;

/**
 * Created by shu on 4/23/2014.
 */

/*
- get 	  /folder/id
- put 	  /folder/id
- delete	/folder/id
- post 	  /folder/id
- post	  /folder
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
		//MediaFolder folder = ebean.find(MediaFolder.class, id);
		//return folder.toString();
		MediaFolder folder = new MediaFolder("xxx");
		folder.setDescription("desc");
		return folder;
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

	@POST
	@Path("/")
	public String getSubSolders() {
		return manager.dump(null, 0);
	}

}
