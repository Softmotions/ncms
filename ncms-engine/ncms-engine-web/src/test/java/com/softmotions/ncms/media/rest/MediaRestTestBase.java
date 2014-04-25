package com.softmotions.ncms.media.rest;

import com.softmotions.ncms.media.db.MediaRestTest;
import com.softmotions.ncms.media.model.MediaFile;
import com.softmotions.ncms.media.model.MediaFolder;
import com.softmotions.ncms.media.model.Tag;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by shu on 4/24/2014.
 */
public class MediaRestTestBase extends MediaRestTest {

	public MediaRestTestBase(String basePath) {
		super(basePath);
	}

	protected ResteasyWebTarget target(long id) {
		return getWebTarget("/" + id);
	}

	protected ResteasyWebTarget target(MediaFolder folder) {
		return target(folder.getId());
	}

	protected ResteasyWebTarget target(MediaFile file) {
		return target(file.getId());
	}

	protected ResteasyWebTarget target(Tag tag) {
		return target(tag.getId());
	}

	protected Entity<MediaFolder> entity(MediaFolder folder) {
		return Entity.entity(folder, "application/json");
	}

	protected Entity<Tag> entity(Tag tag) {
		return Entity.entity(tag, "application/json");
	}

	protected Entity<MediaFile> entity(MediaFile file) {
		return Entity.entity(file, "application/json");
	}

	protected MediaFolder createAndCheck(MediaFolder root, MediaFolder folder) {
		ResteasyWebTarget target = getWebTarget("/folder", root == null? "/" : "/" + root.getId());
		Response response = target.request().post(entity(folder));
		assertEquals(200, response.getStatus());
		MediaFolder f = response.readEntity(MediaFolder.class);
		response.close();
		assertEquals(folder.getName(), f.getName());
		assertEquals(folder.getDescription(), f.getDescription());
		return f;
	}

	protected MediaFolder createAndCheck(MediaFolder folder) {
		return createAndCheck(null, folder);
	}
	protected Tag createAndCheck(Tag tag) {
		ResteasyWebTarget target = getWebTarget("/tag", "/");
		Response response = target.request().post(entity(tag));
		assertEquals(200, response.getStatus());
		Tag t = response.readEntity(Tag.class);
		response.close();
		assertEquals(tag.getName(), t.getName());
		return t;
	}

	protected MediaFile createAndCheck(MediaFolder folder, MediaFile file) {
		ResteasyWebTarget target = getWebTarget("/file", "/"+folder.getId());
		Response response = target.request().post(entity(file));
		assertEquals(200, response.getStatus());
		MediaFile f = response.readEntity(MediaFile.class);
		response.close();
		assertEquals(file.getName(), f.getName());
		assertEquals(file.getDescription(), f.getDescription());
		return f;
	}

	protected MediaFolder requestAndCheck(MediaFolder folder) {
		Response response = getWebTarget("/folder", "/"+folder.getId()).request().get();
		assertEquals(200, response.getStatus());
		MediaFolder f = response.readEntity(MediaFolder.class);
		response.close();
		assertEquals(folder.getName(), f.getName());
		assertEquals(folder.getDescription(), f.getDescription());
		return f;
	}

	protected MediaFile requestAndCheck(MediaFile file) {
		Response response = getWebTarget("/file", "/"+file.getId()).request().get();
		assertEquals(200, response.getStatus());
		MediaFile f = response.readEntity(MediaFile.class);
		response.close();
		assertEquals(file.getName(), f.getName());
		assertEquals(file.getDescription(), f.getDescription());
		return f;
	}

	protected Tag requestAndCheck(Tag tag) {
		Response response = getWebTarget("/tag", "/"+tag.getId()).request().get();
		assertEquals(200, response.getStatus());
		Tag t = response.readEntity(Tag.class);
		response.close();
		assertEquals(tag.getName(), t.getName());
		return t;
	}

	protected void requestAndCheckNotExists(MediaFolder folder) {
		Response response = getWebTarget("/folder", "/"+folder.getId()).request().get();
		assertEquals(404, response.getStatus());
	}

	protected void requestAndCheckNotExists(MediaFile file) {
		Response response = getWebTarget("/file", "/"+file.getId()).request().get();
		assertEquals(404, response.getStatus());
	}

	protected void requestAndCheckNotExists(Tag tag) {
		Response response = getWebTarget("/tag", "/"+tag.getId()).request().get();
		assertEquals(404, response.getStatus());
	}

	protected List<MediaFolder> listFoldersAndCheck(int expectedSize) {
		Response response = getWebTarget("/folders", "/").request().get();
		//String err = response.readEntity(String.class);
		assertEquals(200, response.getStatus());
		List<MediaFolder> list = response.readEntity(List.class);
		assertEquals(expectedSize, list.size());
		response.close();
		return list;
	}

	protected List<Tag> listTagsAndCheck(int expectedSize) {
		Response response = getWebTarget("/tag", "/").request().get();
		//String err = response.readEntity(String.class);
		assertEquals(200, response.getStatus());
		List<Tag> list = response.readEntity(List.class);
		assertEquals(expectedSize, list.size());
		response.close();
		return list;
	}

	protected List<MediaFolder> listFoldersAndCheck(MediaFolder folder, int expectedSize) {
		Response response = target(folder).request().get();
		assertEquals(200, response.getStatus());
		List<MediaFolder> list = response.readEntity(List.class);
		assertEquals(expectedSize, list.size());
		response.close();
		return list;
	}

}
