package com.softmotions.ncms.media.rest;

import com.softmotions.ncms.media.model.MediaFile;
import com.softmotions.ncms.media.model.MediaFolder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Files manipulation API <p>
 *
 * get /files/id - list folder files <br>
 */

@Path("media/files")
public class MediaFilesRS extends MediaRestBase {

	@GET
	@Path("/{folderId}")
	@Produces("application/json")
	public Response listFiles(@PathParam("folderId") Long folderId) {
		MediaFolder folder = ebean.find(MediaFolder.class, folderId);
		if(folder == null) return response(500, "Folder not found: " + folderId);
		List<MediaFile> files = manager.getFiles(folder);
		return ok(files);
	}

}
