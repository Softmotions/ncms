package com.softmotions.ncms.media.rest;

import com.softmotions.ncms.media.model.MediaFile;
import com.softmotions.ncms.media.model.MediaFolder;
import com.softmotions.ncms.media.model.Tag;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by shu on 4/25/2014.
 */

/*
 + post   /tag
 + get    /tag
 + get    /tag/id
 + delete /tag/id

 + get    /tag/file/id/id
 + get    /tag/folder/id/id
 + delete /tag/file/id/id
 + delete /tag/folder/id/id

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
