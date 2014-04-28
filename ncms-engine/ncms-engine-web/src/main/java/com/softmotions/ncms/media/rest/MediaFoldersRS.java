package com.softmotions.ncms.media.rest;

import com.softmotions.ncms.media.model.MediaFolder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;

/**
 * Created by shu on 4/23/2014.
 */

/**
 * Folders manipulation API <p>
 *
 * get 	/folders/show   - test call to show hierarchy <br>
 * get 	/folders        - list root folders <br>
 * get 	/folders/id     - list subfolders <br>
 * get	/folders/id/id  - move folder to another location <br>
*/
@Path("media/folders")
public class MediaFoldersRS extends MediaRestBase {

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
		return ok(folders);
	}

	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response getSubSolders(@PathParam("id") Long id) {
		MediaFolder folder = ebean.find(MediaFolder.class, id);
		if(folder == null) {
			return response(500, "Folder not found: " + id);
		}
		return ok(manager.getSubFolders(folder));
	}

	@GET
	@Path("/{id}/{hostId}")
	public Response moveFolder(@PathParam("id") Long id, @PathParam("hostId") Long hostId) {
		MediaFolder folder = ebean.find(MediaFolder.class, id);
		if(folder == null) {
			return response(500, "Folder not found: " + id);
		}
		MediaFolder host = ebean.find(MediaFolder.class, hostId);
		if(folder == null) {
			return response(500, "Folder not found: " + hostId);
		}
		if(Objects.equals(folder,host)) {
			return response(500, "Couldn't move to itself: " + hostId);
		}
		if(folder.isParentOf(host)) {
			return response(500, "Couldn't move to own subtree: " + hostId);
		}
		folder.setParent(host);
		ebean.update(folder);
		return ok("moved: " + id);
	}

}
