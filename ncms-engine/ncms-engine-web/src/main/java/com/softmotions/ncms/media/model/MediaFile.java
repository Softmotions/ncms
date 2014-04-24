package com.softmotions.ncms.media.model;

import com.avaje.ebean.annotation.PrivateOwned;
import com.google.common.collect.Lists;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: shu
 * Date: 4/12/14
 * Time: 5:25 PM
 * To change this template use File | Settings | File Templates.
 */

@Entity
public class MediaFile {

	@Id
	Long id;

	String name;
	String description;
	String filePath;

	@ManyToMany(cascade = CascadeType.ALL)
	List<Tag> tags = Lists.newArrayList();

	@ManyToOne(cascade=CascadeType.ALL)
	MediaFolder mediaFolder;

	public MediaFile() {
	}

	public MediaFile(String name) {
		this.name = name;
		this.description = "desc";
		this.filePath = "path";
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public void addTag(Tag tag) {
		tags.add(tag);
	}

	public boolean deleteTag(Tag tag) {
		boolean ok = tags.remove(tag);
		return ok;
	}

	public boolean hasTag(Tag tag) {
		return tags.contains(tag);
	}

	@Override
	public String toString() {
		return com.google.common.base.Objects.toStringHelper(this)
						.add("id", id)
						.add("name", name)
						.add("description", description)
						.add("filePath", filePath)
						.add("tags", tags)
						.toString();
	}




	public MediaFolder getMediaFolder() {
		return mediaFolder;
	}

	public void setMediaFolder(MediaFolder mediaFolder) {
		this.mediaFolder = mediaFolder;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!MediaFile.class.isInstance(o)) return false;

		MediaFile that = (MediaFile) o;

		return com.google.common.base.Objects.equal(this.id, that.id);
	}

	@Override
	public int hashCode() {
		return com.google.common.base.Objects.hashCode(id);
	}

	public static MediaFile of(String name, String description) {
		MediaFile file = new MediaFile(name);
		file.setDescription(description);
		return file;
	}
}
