package com.softmotions.ncms.media.model;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

/**
 * Created by shu on 4/20/2014.
 */
@Entity
public class Tag {

	@Id
	long id;

	String name;

	public Tag() {
	}

	public Tag(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static Tag of(String name) {
		return new Tag(name);
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!Tag.class.isInstance(o)) return false;

		Tag that = (Tag) o;
		return Objects.equal(this.name, that.name);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
						.add("id", id)
						.add("name", name)
						.toString();
	}
}
