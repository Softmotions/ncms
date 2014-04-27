package com.softmotions.ncms.media.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;
import java.util.Set;

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

	@ManyToOne
	MediaFolder parent;

	public MediaFolder() {
	}

	public MediaFolder(String name) {
		this.name = name;
		this.description = "desc";
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
		return tags.remove(tag);
	}

	public boolean hasTag(Tag tag) {
		return tags.contains(tag);
	}

	//public void deleteMediaFile(MediaFile file) {
	//	file.setMediaFolder(null);
	//}

	public void addMediaFile(MediaFile file) {
		file.setMediaFolder(this);
	}

	@JsonIgnore
	public MediaFolder getParent() {
		return parent;
	}

	public void setParent(MediaFolder parent) {
		this.parent = parent;
	}



	@Override
	public String toString() {
		return Objects.toStringHelper(this)
						.add("id", id)
						.add("name", name)
						.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!MediaFolder.class.isInstance(o)) return false;


		MediaFolder that = (MediaFolder) o;

		return Objects.equal(this.id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@JsonIgnore
	public Set<MediaFolder> getParents() {
		MediaFolder folder = this;
		Set<MediaFolder> parents = Sets.newHashSet();
		while(folder.getParent() != null) {
			parents.add(folder.getParent());
			folder = folder.getParent();
		}
		return parents;
	}

	public boolean isParentOf(MediaFolder host) {
		return host.getParents().contains(this);
	}

	public boolean isParent(MediaFolder host) {
		return getParents().contains(host);
	}

	public static MediaFolder of(String name, String description) {
		MediaFolder folder = new MediaFolder(name);
		folder.setDescription(description);
		return folder;
	}

}
