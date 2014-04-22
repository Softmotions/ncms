package com.softmotions.ncms.media.model;

import com.avaje.ebean.annotation.PrivateOwned;
import com.google.common.collect.Lists;

import javax.persistence.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: shu
 * Date: 4/12/14
 * Time: 5:25 PM
 * To change this template use File | Settings | File Templates.
 */

@Entity
public class MediaFolder {

	@Id
  Long id;

  String name;
  String description;

	@ManyToMany(cascade = CascadeType.ALL)
  List<Tag> tags;

  //List<MediaFolder> folders;

	//@PrivateOwned
	//@OneToMany(cascade = CascadeType.ALL, mappedBy = "mediaFolder")
	//List<MediaFile> mediaFiles = Lists.newArrayList();

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
		System.out.println("Tags1: " + tags);
		boolean ok = tags.remove(tag);
		System.out.println("Tags2: " + tags);
		return ok;
	}

	public boolean hasTag(Tag tag) {
		return tags.contains(tag);
	}

	public void deleteMediaFile(MediaFile file) {
		file.setMediaFolder(null);
	}

	public void addMediaFile(MediaFile file) {
		file.setMediaFolder(this);
	}

}
