package com.softmotions.ncms.media_old.rest;

import com.softmotions.ncms.media_old.model.MediaFile;
import com.softmotions.ncms.media_old.model.MediaFolder;
import com.softmotions.ncms.media_old.model.Tag;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by shu on 4/25/2014.
 */

/**
 * Tags manupulation API
 *
 * post   /tag      - crete tag <br>
 * get    /tag      - list tags <br>
 * get    /tag/id   - get tag data <br>
 * delete /tag/id   - delete tag <br>
 *
 * get    /tag/file/id/id     - add file tag <br>
 * get    /tag/folder/id/id   - add folder tag <br>
 * delete /tag/file/id/id     - delete file tag <br>
 * delete /tag/folder/id/id   - delete folder tag <br>
*/

@Path("media/tag")
public class TagRS extends MediaRestBase {

	@POST
	@Path("/")
	@Consumes("application/json")
	@Produces("application/json")
	public Response createTag(Tag tag) {
		ebean.save(tag);
		return ok(tag);
	}

	@GET
	@Path("/")
	@Produces("application/json")
	public Response getTags() {
		List<Tag> tags = ebean.find(Tag.class).findList();
		return ok(tags);
	}

	@DELETE
	@Path("/{id}")
	public Response deleteTag(@PathParam("id") Long id) {
		Tag tag = ebean.find(Tag.class, id);
		if(tag == null) response(500, "Tag not found: " + id);
		ebean.delete(tag);
		return ok("deleted: " + id);
	}

	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response getTag(@PathParam("id") Long id) {
		Tag tag = ebean.find(Tag.class, id);
		System.out.println("GET TAG: " + id + ": " + tag);
		if(tag == null) return response(404, "Tag not found: " + id);
		return ok(tag);
	}

	@GET
	@Path("/folder/{folderId}/{tagId}")
	public Response addFolderTag(@PathParam("folderId") Long folderId, @PathParam("tagId") Long tagId) {
		Tag tag = ebean.find(Tag.class, tagId);
		MediaFolder folder = ebean.find(MediaFolder.class, folderId);
		if(tag == null) return response(500, "Tag not found: " + tagId);
		if(folder == null) return response(500, "Folder not found: " + folderId);
		if(folder.getTags().contains(tag)) {
			return response(500, "Folder already tagged with " + tag.getName());
		} else {
			folder.getTags().add(tag);
			ebean.update(folder);
			return ok("Folder tagged with " + tag.getName());
		}
	}

	@GET
	@Path("/file/{fileId}/{tagId}")
	public Response addFileTag(@PathParam("fileId") Long fileId, @PathParam("tagId") Long tagId) {
		Tag tag = ebean.find(Tag.class, tagId);
		MediaFile file = ebean.find(MediaFile.class, fileId);
		if(tag == null) return response(500, "Tag not found: " + tagId);
		if(file == null) return response(500, "File not found: " + fileId);
		if(file.getTags().contains(tag)) {
			return response(500, "File already tagged with " + tag.getName());
		} else {
			file.getTags().add(tag);
			ebean.update(file);
			return ok("File tagged with " + tag.getName());
		}
	}

	@DELETE
	@Path("/folder/{folderId}/{tagId}")
	public Response deleteFolderTag(@PathParam("folderId") Long folderId, @PathParam("tagId") Long tagId) {
		Tag tag = ebean.find(Tag.class, tagId);
		MediaFolder folder = ebean.find(MediaFolder.class, folderId);
		if(tag == null) return response(500, "Tag not found: " + tagId);
		if(folder == null) return response(500, "Folder not found: " + folderId);
		if(!folder.getTags().contains(tag)) return response(500, "No tag " + tag.getName() + " associated with folder: " + folderId);
		folder.getTags().remove(tag);
		ebean.update(folder);
		return ok("Folder untagged: " + tag.getName());
	}

	@DELETE
	@Path("/file/{fileId}/{tagId}")
	public Response deleteFileTag(@PathParam("fileId") Long fileId, @PathParam("tagId") Long tagId) {
		Tag tag = ebean.find(Tag.class, tagId);
		MediaFile file = ebean.find(MediaFile.class, fileId);
		if(tag == null) return response(500, "Tag not found: " + tagId);
		if(file == null) return response(500, "File not found: " + fileId);
		if(!file.getTags().contains(tag)) return response(500, "No tag " + tag.getName() + " associated with file: " + fileId);
		file.getTags().remove(tag);
		ebean.update(file);
		return ok("File untagged: " + tag.getName());
	}

}
