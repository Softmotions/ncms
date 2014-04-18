package com.softmotions.ncms.media.model;

import java.util.List;
import java.util.Objects;
import com.avaje.ebean.Ebean;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: shu
 * Date: 4/12/14
 * Time: 5:25 PM
 * To change this template use File | Settings | File Templates.
 */

@Entity
public class MediaFile {

  @Id  Long id;

  String name;
  String description;
  String filePath;

  //List<String> tags;

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

//  public List<String> getTags() {
//    return tags;
//  }

//  public void setTags(List<String> tags) {
//    this.tags = tags;
//  }

  @Override
  public boolean equals(Object obj) {
    if(!MediaFile.class.isAssignableFrom(obj.getClass())) return false;
    MediaFile mf = MediaFile.class.cast(obj);
    if(mf == null) return false;
    return Objects.equals(name, mf.name) && Objects.equals(description, mf.description) && Objects.equals(filePath, mf.filePath);
  }
}
